/**
 *
 */
package com.solgroup.job.importData.jobs;


import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Stopwatch;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.dao.b2bunit.SolGroupB2BUnitDao;
import com.solgroup.core.enums.B2BUnitTypeEnum;
import com.solgroup.core.model.job.BatchImportCronJobModel;
import com.solgroup.core.service.address.SolGroupAddressService;
import com.solgroup.core.service.b2bunit.impl.DefaultSolGroupB2BUnitService;
import com.solgroup.core.ws.services.customer.xml.Address;
import com.solgroup.core.ws.services.customer.xml.B2BUnits.B2BUnit;
import com.solgroup.core.ws.services.customer.xml.Companies;
import com.solgroup.core.ws.services.customer.xml.Companies.Company;
import com.solgroup.core.ws.services.customer.xml.CustomerImportHybris;
import com.solgroup.core.ws.services.customer.xml.Organizations1.OrganizationUnit1;
import com.solgroup.core.ws.services.customer.xml.Organizations2.OrganizationUnit2;
import com.solgroup.job.importData.abstractJob.AbstractImportJob;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.i18n.daos.CountryDao;
import de.hybris.platform.servicelayer.i18n.daos.CurrencyDao;
import jersey.repackaged.com.google.common.collect.Maps;

/**
 * @author mdc
 *
 */
public class ImportCustomerJob extends AbstractImportJob {

	private CountryDao countryDao;
	private CurrencyDao currencyDao;
	private SolGroupB2BUnitDao solGroupB2BUnitDao;
	private DefaultSolGroupB2BUnitService solGroupB2BUnitService;
	private SolGroupAddressService solGroupAddressService;
	
	
	private final Logger LOG = Logger.getLogger(ImportCustomerJob.class);
	
	private static final String IMPEX_SEPARATOR = ";";
	private static final String STRING_RETURN = "\n";
	
	private static final String impexHeader_company = "INSERT_UPDATE Company;	uid[unique=true];	erpCode;	name[default=<ignore>];	locName[lang=%s,default=<ignore>];locName[lang=%s,default=<ignore>];	cmsSite(uid);";
	private static final String impexHeader_defaultCompany = "UPDATE CMSSite;	uid[unique=true];	defaultCompany(uid);";
	
	private static final String impexHeader_organizationUnit1 = "INSERT_UPDATE B2BUnit;	uid[unique=true];	erpCode;	b2bUnitType(code)[default=ORGANIZATION_1LEVEL];	legalEntity(uid);	name;locName[lang=%s,default=<ignore>];locName[lang=%s,default=<ignore>];	responsibleCompany(uid);	cmsSite(uid);	sessionCurrency(isocode);	&b2bUnitId;";
	private static final String impexHeader_organizationUnit2 = "INSERT_UPDATE B2BUnit;	uid[unique=true];	erpCode;	b2bUnitType(code)[default=ORGANIZATION_2LEVEL];	legalEntity(uid);	name;locName[lang=%s,default=<ignore>];locName[lang=%s,default=<ignore>];	responsibleCompany(uid);	cmsSite(uid);	sessionCurrency(isocode);	&b2bUnitId;";
	private static final String impexHeader_b2bUnit = "INSERT_UPDATE B2BUnit;	uid[unique=true];	erpCode;	b2bUnitType(code)[default=POINT_OF_SALE];	legalEntity(uid);	name[default=<ignore>];locName[lang=%s,default=<ignore>];locName[lang=%s,default=<ignore>];	responsibleCompany(uid);	cmsSite(uid);	sessionCurrency(isocode);	vatId[default=<ignore>]; taxPayerCode[default=<ignore>];	publicClient[default=<ignore>];	creditState(code)[default=<ignore>];	vendorCode[default=<ignore>];	clientType(code)[default=<ignore>];	active; &b2bUnitId"; 
	
	private static final String impexHeader_address_withoutPk = "INSERT_UPDATE Address;	 erpCode[unique=true];	duplicate[default=false,unique=true];	streetName[default=<ignore>];	streetNumber[default=<ignore>];	town[default=<ignore>];	postalCode[default=<ignore>];	country(isocode)[default=<ignore>];	email[default=<ignore>];	phone1[default=<ignore>];	fax[default=<ignore>];	shippingAddress[default=<ignore>];	billingAddress[default=<ignore>];	owner(&b2bUnitId)[unique=true];";
	// TODO da ricontrollare quando si implementerà l'import dal legacy di address con l'hybris code valorizzato
	private static final String impexHeader_address_withPk = "INSERT_UPDATE Address;	hybrisCode[unique=true];	erpCode[unique=true];	duplicate[default=false,unique=true];	streetName[default=<ignore>];	streetNumber[default=<ignore>];	town[default=<ignore>];	postalCode[default=<ignore>];	country(isocode)[default=<ignore>];	email[default=<ignore>];	phone1[default=<ignore>];	fax[default=<ignore>];	shippingAddress[default=<ignore>];	billingAddress[default=<ignore>];	owner(&b2bUnitId)[unique=true];";
	
	private static final String impexHeader_updateB2bUnitAddress = "UPDATE B2BUnit; uid[unique=true];	shippingAddress(erpCode,duplicate[default=false]);	billingAddress(erpCode,duplicate[default=false]);";
	
	private static final String impexHeader_b2bCustomer = "INSERT_UPDATE B2BCustomer;	uid[unique=true];	name;	email;	cmsSite(uid);	defaultB2BUnit(uid);";
	
	@Override
	public PerformResult perform(final CronJobModel cronJob) {
		if (cronJob instanceof BatchImportCronJobModel) {
			
			Stopwatch timer = Stopwatch.createStarted();

			final BatchImportCronJobModel batchImportCronJob = (BatchImportCronJobModel) cronJob;
			final MediaModel xmlMedia = batchImportCronJob.getImportMedia();
			final InputStream mediaStream = getMediaService().getStreamFromMedia(xmlMedia);
			final CustomerImportHybris customerImport = (CustomerImportHybris) getCommonWsService()
					.parseXmlStream(CustomerImportHybris.class, mediaStream);

			final CMSSiteModel site = batchImportCronJob.getSite();
			if (site == null) {
				LOG.error("No cmsSite found for BatchImportCronJob " + batchImportCronJob.getCode());
				timer.stop();
				LOG.info("Elapsed time = " + timer.elapsed(TimeUnit.SECONDS));
				return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}

			final CountryModel country = batchImportCronJob.getCountry();

			LOG.info("Start importCustomer job " + batchImportCronJob.getCode());

			Boolean result = processDataToImport(customerImport, site, country, batchImportCronJob.getJob().getCode());

			LOG.info("End importCustomer job " + batchImportCronJob.getCode() + ". Has errors = " + !result);

			timer.stop();
			LOG.info("Elapsed time = " + timer.elapsed(TimeUnit.SECONDS));

			if(result) {
				return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
			}
			else {
				return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}
		}

		return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
	}


	private Boolean processDataToImport(final CustomerImportHybris customerImport, final CMSSiteModel cmsSite,
			final CountryModel country, String jobCode) {

		// Get locale and defaultLocale
		Locale locale = Locale.ITALY;
		Locale defaultLocale = Locale.ENGLISH;
		final Map<String, Locale> localesMap = getSolGroupUtilities().getLocalesMap();
		if (localesMap.containsKey(country.getIsocode()) && localesMap.get(country.getIsocode()) != null) {
			locale = localesMap.get(country.getIsocode());
		}
		if (cmsSite.getDefaultLanguage()!=null && localesMap.containsKey(cmsSite.getDefaultLanguage().getIsocode()) && localesMap.get(cmsSite.getDefaultLanguage().getIsocode()) != null) {
			defaultLocale = localesMap.get(cmsSite.getDefaultLanguage().getIsocode());
		}

		LOG.info("Locale language is " + locale.getLanguage());
		LOG.info("Default locale language is " + defaultLocale.getLanguage());
		
		StringBuilder companyStringBuilder = new StringBuilder();
		companyStringBuilder.append(String.format(impexHeader_company, defaultLocale.getLanguage(), locale.getLanguage()));
		companyStringBuilder.append(STRING_RETURN);
		
		StringBuilder defaultCompanyBuilder = new StringBuilder();
		defaultCompanyBuilder.append(impexHeader_defaultCompany);
		defaultCompanyBuilder.append(STRING_RETURN);
		
		StringBuilder organizationUnit1Builder = new StringBuilder();
		organizationUnit1Builder.append(String.format(impexHeader_organizationUnit1, defaultLocale.getLanguage(), locale.getLanguage()));
		organizationUnit1Builder.append(STRING_RETURN);

		StringBuilder organizationUnit2Builder = new StringBuilder();
		organizationUnit2Builder.append(String.format(impexHeader_organizationUnit2, defaultLocale.getLanguage(), locale.getLanguage()));
		organizationUnit2Builder.append(STRING_RETURN);
		
		StringBuilder b2bUnitBuilder = new StringBuilder();
		b2bUnitBuilder.append(String.format(impexHeader_b2bUnit, defaultLocale.getLanguage(), locale.getLanguage()));
		b2bUnitBuilder.append(STRING_RETURN);
		
		// TODO da ripristinare quando si implementa l'improt dal legacy di address con l'erpCode valorizzato
		StringBuilder address_withPk_Builder = new StringBuilder();
		address_withPk_Builder.append(impexHeader_address_withPk);
		address_withPk_Builder.append(STRING_RETURN);
		
		StringBuilder address_withoutPk_Builder = new StringBuilder();
		address_withoutPk_Builder.append(impexHeader_address_withoutPk);
		address_withoutPk_Builder.append(STRING_RETURN);
		
		StringBuilder updateB2bUnitAddress_Builder = new StringBuilder();
		updateB2bUnitAddress_Builder.append(impexHeader_updateB2bUnitAddress);
		updateB2bUnitAddress_Builder.append(STRING_RETURN);
		
		StringBuilder b2bCustomerBuidler = new StringBuilder();
		b2bCustomerBuidler.append(impexHeader_b2bCustomer);
		b2bCustomerBuidler.append(STRING_RETURN);
		
		
		// Iterate over all company
		final Companies companies = customerImport.getCompanies();
		for (final Company company : companies.getCompany()) {
			
			createCompanyImpexRow(company, cmsSite, companyStringBuilder, defaultCompanyBuilder);
			
			// Iterate over all 1level organizaions
			if(company.getOrganizations1()!=null && CollectionUtils.isNotEmpty(company.getOrganizations1().getOrganizationUnit1())) {
				for(OrganizationUnit1 organization1 : company.getOrganizations1().getOrganizationUnit1()) {
					createOrganization1ImpexRow(organization1, company, cmsSite, organizationUnit1Builder);
					
					// Iterate over all 2level organizations
					if(organization1.getOrganizations2()!=null && CollectionUtils.isNotEmpty(organization1.getOrganizations2().getOrganizationUnit2())) {
						for(OrganizationUnit2 organization2 : organization1.getOrganizations2().getOrganizationUnit2()) {
							createOrganization2ImpexRow(organization2, organization1, company, cmsSite, organizationUnit2Builder);
							
							// Iterate over all b2bUnits into organization_2level
							if(organization2.getB2BUnits()!=null && CollectionUtils.isNotEmpty(organization2.getB2BUnits().getB2BUnit())) {
								for(B2BUnit b2bUnit : organization2.getB2BUnits().getB2BUnit()) {
									createB2BUnitImpexRow(b2bUnit, getOrganizationCode(organization2, company, cmsSite), company, cmsSite, b2bUnitBuilder);
									// Iterate over all addresses related with b2bUnit
									if(b2bUnit.getAddresses()!=null && CollectionUtils.isNotEmpty(b2bUnit.getAddresses().getAddress())) {
										for(Address address : b2bUnit.getAddresses().getAddress()) {
											if(StringUtils.isNotEmpty(address.getHybrisCode())) {
												createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withPk_Builder);
											}
											else {
												createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withoutPk_Builder);
											}
//											createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withoutPk_Builder);
											
										}
										createUpdateB2BUnitAddressImpexRow(b2bUnit.getAddresses().getAddress(), getB2BUnitCode(b2bUnit, company, cmsSite), updateB2bUnitAddress_Builder);
									}
									// Iterate over all b2bCustomer related with current b2bUnit
									if(b2bUnit.getCustomers()!=null && CollectionUtils.isNotEmpty(b2bUnit.getCustomers().getCustomerId())) {
										for(String customerId : b2bUnit.getCustomers().getCustomerId()) {
											createB2BCustomerImpexRow(customerId, getB2BUnitCode(b2bUnit, company, cmsSite), cmsSite, b2bCustomerBuidler);
										}
									}
								}
							}
							
							// Iterate over all addresses related with organizaion_2level
							if(organization2.getAddresses()!=null && CollectionUtils.isNotEmpty(organization2.getAddresses().getAddress())) {
								for(Address address : organization2.getAddresses().getAddress()) {
									if(StringUtils.isNotEmpty(address.getHybrisCode())) {
										createAddressImpexRow(address, getOrganizationCode(organization2, company, cmsSite), address_withPk_Builder);
									}
									else {
										createAddressImpexRow(address, getOrganizationCode(organization2, company, cmsSite), address_withoutPk_Builder);
									}
//									createAddressImpexRow(address, getOrganizationCode(organization2, company, cmsSite), address_withoutPk_Builder);
								}
								createUpdateB2BUnitAddressImpexRow(organization2.getAddresses().getAddress(), getOrganizationCode(organization2, company, cmsSite), updateB2bUnitAddress_Builder);
							}
							// Iterate over all b2bCustomer related with current organization_2levle
							if(organization2.getCustomers()!=null && CollectionUtils.isNotEmpty(organization2.getCustomers().getCustomerId())) {
								for(String customerId : organization2.getCustomers().getCustomerId()) {
									createB2BCustomerImpexRow(customerId, getOrganizationCode(organization2, company, cmsSite), cmsSite, b2bCustomerBuidler);
								}
							}
							
						}
					}
					
					// Iterate over all b2bUnits into organization 1level 
					if(organization1.getB2BUnits()!=null && CollectionUtils.isNotEmpty(organization1.getB2BUnits().getB2BUnit())) {
						for(B2BUnit b2bUnit : organization1.getB2BUnits().getB2BUnit()) {
							createB2BUnitImpexRow(b2bUnit, getOrganizationCode(organization1, company, cmsSite), company, cmsSite, b2bUnitBuilder);
							// Iterate over all addresses related with b2bUnit
							if(b2bUnit.getAddresses()!=null && CollectionUtils.isNotEmpty(b2bUnit.getAddresses().getAddress())) {
								for(Address address : b2bUnit.getAddresses().getAddress()) {
									if(StringUtils.isNotEmpty(address.getHybrisCode())) {
										createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withPk_Builder);
									}
									else {
										createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withoutPk_Builder);
									}
//									createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withoutPk_Builder);
								}
								
								createUpdateB2BUnitAddressImpexRow(b2bUnit.getAddresses().getAddress(), getB2BUnitCode(b2bUnit, company, cmsSite), updateB2bUnitAddress_Builder);
							}
							// Iterate over all b2bCustomer related with current b2bUnit
							if(b2bUnit.getCustomers()!=null && CollectionUtils.isNotEmpty(b2bUnit.getCustomers().getCustomerId())) {
								for(String customerId : b2bUnit.getCustomers().getCustomerId()) {
									createB2BCustomerImpexRow(customerId, getB2BUnitCode(b2bUnit, company, cmsSite), cmsSite, b2bCustomerBuidler);
								}
							}
						}
					}
					
					// Iterate over all addresses related with organizaion_1level
					if(organization1.getAddresses()!=null && CollectionUtils.isNotEmpty(organization1.getAddresses().getAddress())) {
						for(Address address : organization1.getAddresses().getAddress()) {
							if(StringUtils.isNotEmpty(address.getHybrisCode())) {
								createAddressImpexRow(address, getOrganizationCode(organization1, company, cmsSite), address_withPk_Builder);
							}
							else {
								createAddressImpexRow(address, getOrganizationCode(organization1, company, cmsSite), address_withoutPk_Builder);
							}
//							createAddressImpexRow(address, getOrganizationCode(organization1, company, cmsSite), address_withoutPk_Builder);
							
						}
						createUpdateB2BUnitAddressImpexRow(organization1.getAddresses().getAddress(), getOrganizationCode(organization1, company, cmsSite), updateB2bUnitAddress_Builder);
					}
					// Iterate over all b2bCustomer related with current organization_1level
					if(organization1.getCustomers()!=null && CollectionUtils.isNotEmpty(organization1.getCustomers().getCustomerId())) {
						for(String customerId : organization1.getCustomers().getCustomerId()) {
							createB2BCustomerImpexRow(customerId, getOrganizationCode(organization1, company, cmsSite), cmsSite, b2bCustomerBuidler);
						}
					}
					
				}
			}
			
			// Iterate over all b2bUnits into company
			if(company.getB2BUnits()!=null && CollectionUtils.isNotEmpty(company.getB2BUnits().getB2BUnit())) {
				for(B2BUnit b2bUnit : company.getB2BUnits().getB2BUnit()) {
					createB2BUnitImpexRow(b2bUnit, getCompanyCode(company, cmsSite), company, cmsSite, b2bUnitBuilder);
					// Iterate over all addresses related with b2bUnit
					if(b2bUnit.getAddresses()!=null && CollectionUtils.isNotEmpty(b2bUnit.getAddresses().getAddress())) {
						for(Address address : b2bUnit.getAddresses().getAddress()) {
							if(StringUtils.isNotEmpty(address.getHybrisCode())) {
								createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withPk_Builder);
							}
							else {
								createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withoutPk_Builder);
							}
//							createAddressImpexRow(address, getB2BUnitCode(b2bUnit, company, cmsSite), address_withoutPk_Builder);
						}
						createUpdateB2BUnitAddressImpexRow(b2bUnit.getAddresses().getAddress(), getB2BUnitCode(b2bUnit, company, cmsSite), updateB2bUnitAddress_Builder);
					}
					// Iterate over all b2bCustomer related with current b2bUnit
					if(b2bUnit.getCustomers()!=null && CollectionUtils.isNotEmpty(b2bUnit.getCustomers().getCustomerId())) {
						for(String customerId : b2bUnit.getCustomers().getCustomerId()) {
							createB2BCustomerImpexRow(customerId, getB2BUnitCode(b2bUnit, company, cmsSite), cmsSite, b2bCustomerBuidler);
						}
					}
				}
			}
			
		}

		
		StringBuilder totalImpex = new StringBuilder();
		totalImpex.append(companyStringBuilder.toString());
		totalImpex.append(organizationUnit1Builder.toString());
		totalImpex.append(organizationUnit2Builder.toString());
		totalImpex.append(b2bUnitBuilder.toString());
		totalImpex.append(address_withoutPk_Builder.toString());
		totalImpex.append(address_withPk_Builder.toString());
		totalImpex.append(updateB2bUnitAddress_Builder.toString());
		totalImpex.append(b2bCustomerBuidler.toString());
		totalImpex.append(defaultCompanyBuilder);
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("\n" + totalImpex.toString());
		}

		
		try {
			
			performImpexImport(totalImpex.toString(),cmsSite,customerImport.getHeader().getSourceSystem(),customerImport.getHeader().getMessageID(),jobCode);
			
			return Boolean.TRUE;
			
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			
		}
		
		return Boolean.FALSE;
	}
	
	private void createB2BCustomerImpexRow(String b2bCustomerId, String defaultB2BUnitId, CMSSiteModel cmsSite, StringBuilder stringBuilder) {
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// b2bCustomer id
		stringBuilder.append(b2bCustomerId.toLowerCase());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// name
		stringBuilder.append(b2bCustomerId);
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// email
		if(isValidEmailAddress(b2bCustomerId)) {
			stringBuilder.append(b2bCustomerId);
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// cmsSite
		stringBuilder.append(cmsSite.getUid());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// default b2bUnit
		stringBuilder.append(defaultB2BUnitId);
		stringBuilder.append(IMPEX_SEPARATOR);
		
		stringBuilder.append(STRING_RETURN);
	}












	
	private void createAddressImpexRow(Address address, String addressOwnerId, StringBuilder stringBuilder) {
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// TODO da verificare quando si implementa l'import dal legacy di address con l'erpCode valorizzato

		// hybrisCode
		if(StringUtils.isNotEmpty(address.getHybrisCode())) {
			stringBuilder.append(address.getHybrisCode());
			stringBuilder.append(IMPEX_SEPARATOR);
		}
		
		// erp code
		stringBuilder.append(getSolGroupAddressService().getHybrisErpCode(address.getErpCode(), addressOwnerId));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// duplicate
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// street name
		String streetName = address.getStreet();
		streetName = streetName.replaceAll("\"", "\"\"");
		stringBuilder.append("\"" + streetName + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// street number
		stringBuilder.append(address.getStreetNumber());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// town
		stringBuilder.append(address.getTown());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// postal code
		stringBuilder.append(address.getPostalCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// country
		stringBuilder.append(address.getCountry());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// email
		stringBuilder.append(address.getEmail());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// phone1
		stringBuilder.append(address.getPhone());
		stringBuilder.append(IMPEX_SEPARATOR);

		// fax
		stringBuilder.append(address.getFax());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// shippingAddress
		stringBuilder.append(String.valueOf(address.isShipping()));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// billingAddress
		stringBuilder.append(String.valueOf(address.isBilling()));
		stringBuilder.append(IMPEX_SEPARATOR);

		//owner
		stringBuilder.append(addressOwnerId);
		stringBuilder.append(IMPEX_SEPARATOR);

		stringBuilder.append(STRING_RETURN);
		
		
	}
	
	
	private void createUpdateB2BUnitAddressImpexRow(Collection<Address> addresses, String b2bUnitUid, StringBuilder stringBuilder) {
		
		String shippingAddressErpCode = "";
		String billingAddressErpCode = "";
		
		// Shipping address and billingAddress (use last address)
		Map<String,Address> addressMap = null;
			addressMap = parseAddressList(addresses);
		
		if(addressMap!=null && addressMap.containsKey("shippingAddress")) {
			shippingAddressErpCode = getSolGroupAddressService().getHybrisErpCode(addressMap.get("shippingAddress").getErpCode(), b2bUnitUid);
		}
		if(addressMap!=null && addressMap.containsKey("billingAddress")) {
			billingAddressErpCode = getSolGroupAddressService().getHybrisErpCode(addressMap.get("billingAddress").getErpCode(), b2bUnitUid);
		}
		
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(b2bUnitUid);
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(shippingAddressErpCode);
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(billingAddressErpCode);
		stringBuilder.append(IMPEX_SEPARATOR);
		
		stringBuilder.append(STRING_RETURN);
		
	}
	
	
	private void createCompanyImpexRow(Company company, CMSSiteModel site, StringBuilder stringBuilder, StringBuilder defaultCompanyBuilder) {
		
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Company uid
		stringBuilder.append(getCompanyCode(company, site));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// erpCode
		stringBuilder.append(company.getErpCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// name and locName
		if(StringUtils.isNotEmpty(company.getName())) {
			stringBuilder.append(company.getName());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		if(StringUtils.isNotEmpty(company.getName())) {
			stringBuilder.append(company.getName());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		if(StringUtils.isNotEmpty(company.getName())) {
			stringBuilder.append(company.getName());
		}
		stringBuilder.append(IMPEX_SEPARATOR);

		
		// site
		stringBuilder.append(site.getUid());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		stringBuilder.append(STRING_RETURN);
		
		if(company.isDefault()) {
			defaultCompanyBuilder.append(IMPEX_SEPARATOR);
			//site
			defaultCompanyBuilder.append(site.getUid());
			defaultCompanyBuilder.append(IMPEX_SEPARATOR);
			// default company
			defaultCompanyBuilder.append(getCompanyCode(company, site));
			defaultCompanyBuilder.append(IMPEX_SEPARATOR);
			
			defaultCompanyBuilder.append(STRING_RETURN);
		}

	}

	
	private void createOrganization1ImpexRow(OrganizationUnit1 organizationUnit1, Company legalEntity, CMSSiteModel site, StringBuilder stringBuilder) {
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Organization uid
		stringBuilder.append(getOrganizationCode(organizationUnit1,legalEntity,site));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Erp code
		stringBuilder.append(organizationUnit1.getErpCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// B2BUnit type (set as default value)
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Legal entity
		stringBuilder.append(getCompanyCode(legalEntity, site));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Name and locName 
		String name = organizationUnit1.getName();
		name = name.replaceAll("\"", "\"\"");
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Responsible company
		stringBuilder.append(getCompanyCode(legalEntity, site));
		stringBuilder.append(IMPEX_SEPARATOR);

		// site
		stringBuilder.append(site.getUid());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// currency
		if (StringUtils.isNotBlank(organizationUnit1.getCurrency())) {
			stringBuilder.append(organizationUnit1.getCurrency());
		}
		else {
			stringBuilder.append(site.getStores().get(0).getDefaultCurrency().getIsocode());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// b2bUnitId
		stringBuilder.append(getOrganizationCode(organizationUnit1,legalEntity,site));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		stringBuilder.append(STRING_RETURN);
		
	}
	
	private void createOrganization2ImpexRow(OrganizationUnit2 organizationUnit2, OrganizationUnit1 parentOrganization, Company legalEntity, CMSSiteModel site, StringBuilder stringBuilder) {
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Organization uid
		stringBuilder.append(getOrganizationCode(organizationUnit2,legalEntity,site));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Erp code
		stringBuilder.append(organizationUnit2.getErpCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// B2BUnit type (set as default value)
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Legal entity
		stringBuilder.append(getCompanyCode(legalEntity, site));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Name and locName 
		String name = organizationUnit2.getName();
		name = name.replaceAll("\"", "\"\"");
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Responsible company
		stringBuilder.append(getOrganizationCode(parentOrganization, legalEntity, site));
		stringBuilder.append(IMPEX_SEPARATOR);

		// site
		stringBuilder.append(site.getUid());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// currency
		if (StringUtils.isNotBlank(organizationUnit2.getCurrency())) {
			stringBuilder.append(organizationUnit2.getCurrency());
		}
		else {
			stringBuilder.append(site.getStores().get(0).getDefaultCurrency().getIsocode());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// b2bUnitId
		stringBuilder.append(getOrganizationCode(organizationUnit2,legalEntity,site));
		stringBuilder.append(IMPEX_SEPARATOR);

		
		stringBuilder.append(STRING_RETURN);
		
	}
	
	private void createB2BUnitImpexRow(B2BUnit b2bUnit, String parentCode, Company legalEntity, CMSSiteModel site, StringBuilder stringBuilder) {
		
		if(getB2BUnitCode(b2bUnit, legalEntity, site).equals(parentCode)) {
			LOG.warn("B2BUnit with code " + getB2BUnitCode(b2bUnit, legalEntity, site) + " can not be created!! Its parent code is the same.");
			return;
		}
		
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// B2BUnit uid
		stringBuilder.append(getB2BUnitCode(b2bUnit, legalEntity, site));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// erp code
		stringBuilder.append(b2bUnit.getErpCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// B2BUnit type (set as default value)
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// legal entity
		stringBuilder.append(getCompanyCode(legalEntity, site));
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Name and locName
		String name = b2bUnit.getName();
		name = name.replaceAll("\"", "\"\"");
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append("\"" + name + "\"");
		stringBuilder.append(IMPEX_SEPARATOR);

		// responsible company
		stringBuilder.append(parentCode);
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// site
		stringBuilder.append(site.getUid());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// currency
		if (StringUtils.isNotBlank(b2bUnit.getCurrency())) {
			stringBuilder.append(b2bUnit.getCurrency());
		}
		else {
			stringBuilder.append(site.getStores().get(0).getDefaultCurrency().getIsocode());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		//vatId
		stringBuilder.append(b2bUnit.getVatID());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// tax payer code
		stringBuilder.append(b2bUnit.getTaxPayerCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// public client
		if(BooleanUtils.isTrue(b2bUnit.isPublicClient())) {
			stringBuilder.append(String.valueOf(true));
		}
		else {
			stringBuilder.append(String.valueOf(false));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// credit state
		stringBuilder.append(b2bUnit.getCreditState());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// vendor code
		stringBuilder.append(b2bUnit.getVendorCode());
		stringBuilder.append(IMPEX_SEPARATOR);

		// client type
		stringBuilder.append(b2bUnit.getClientType());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		
		// Active state
		String activeState = "";

		// Set active (0,false = inactive, 1,true = active)
		if(b2bUnit.getActiveState()==null || b2bUnit.getActiveState().isEmpty()) {
			activeState = "0";
		}
		else {
			activeState = b2bUnit.getActiveState().toUpperCase();
			activeState = activeState.toUpperCase();
		}
		switch (b2bUnit.getActiveState()) {
		case "0":
			stringBuilder.append(String.valueOf(false));
			break;
		case "1":
			stringBuilder.append(String.valueOf(true));
			break;
		case "TRUE":
			stringBuilder.append(String.valueOf(true));
			break;
		case "FALSE":
			stringBuilder.append(String.valueOf(true));
			break;
		default:
			stringBuilder.append(String.valueOf(true));
			break;
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// b2bUnitId
		stringBuilder.append(getB2BUnitCode(b2bUnit, legalEntity, site));
		stringBuilder.append(IMPEX_SEPARATOR);

		
		stringBuilder.append(STRING_RETURN);
		

	}

	
	private boolean isValidEmailAddress(final String email) {
		boolean result = true;
		try {
			final InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (final AddressException ex) {

			LOG.warn(String.format("Email address '[%s]' is not valid. b2bCustomer [%s] will be skipped !!!", email,
					email));
			result = false;
		}
		return result;
	}

	
	private String getCompanyCode(Company company, CMSSiteModel site) {
		return site.getUid() + "_" + company.getErpCode();
	}
	
	private String getOrganizationCode(OrganizationUnit1 organizationUnit1, Company legalEntity, CMSSiteModel cmsSite) {
		return getSolGroupB2BUnitService().getHybrisCode(legalEntity.getErpCode(), organizationUnit1.getErpCode(), cmsSite, B2BUnitTypeEnum.ORGANIZATION_1LEVEL);
	}

	private String getOrganizationCode(OrganizationUnit2 organizationUnit2, Company legalEntity, CMSSiteModel cmsSite) {
		return getSolGroupB2BUnitService().getHybrisCode(legalEntity.getErpCode(), organizationUnit2.getErpCode(), cmsSite, B2BUnitTypeEnum.ORGANIZATION_2LEVEL);
	}
	
	private String getB2BUnitCode(B2BUnit b2bUnit, Company legalEntity, CMSSiteModel cmsSite) {
		return getSolGroupB2BUnitService().getHybrisCode(legalEntity.getErpCode(), b2bUnit.getErpCode(), cmsSite, B2BUnitTypeEnum.POINT_OF_SALE);
	}
	
	private Map<String,Address> parseAddressList(Collection<Address> addressList) {
		Map<String,Address> result = Maps.newHashMap();
		for(Address address : addressList) {
			if(address.isShipping()) {
				result.put("shippingAddress", address);
			}
			if(address.isBilling()) {
				result.put("billingAddress", address);
			}
		}
		return result;
	}

	
	protected CurrencyDao getCurrencyDao() {
		return currencyDao;
	}

	@Required
	public void setCurrencyDao(final CurrencyDao currencyDao) {
		this.currencyDao = currencyDao;
	}

	protected CountryDao getCountryDao() {
		return countryDao;
	}

	@Required
	public void setCountryDao(final CountryDao countryDao) {
		this.countryDao = countryDao;
	}

	protected DefaultSolGroupB2BUnitService getSolGroupB2BUnitService() {
		return solGroupB2BUnitService;
	}

	@Required
	public void setSolGroupB2BUnitService(DefaultSolGroupB2BUnitService solGroupB2BUnitService) {
		this.solGroupB2BUnitService = solGroupB2BUnitService;
	}

	protected SolGroupB2BUnitDao getSolGroupB2BUnitDao() {
		return solGroupB2BUnitDao;
	}

	@Required
	public void setSolGroupB2BUnitDao(SolGroupB2BUnitDao solGroupB2BUnitDao) {
		this.solGroupB2BUnitDao = solGroupB2BUnitDao;
	}

	protected SolGroupAddressService getSolGroupAddressService() {
		return solGroupAddressService;
	}

	@Required
	public void setSolGroupAddressService(SolGroupAddressService solGroupAddressService) {
		this.solGroupAddressService = solGroupAddressService;
	}

	

	
	

}
