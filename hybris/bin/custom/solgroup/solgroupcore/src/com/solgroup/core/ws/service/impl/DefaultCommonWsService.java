/**
 *
 */
package com.solgroup.core.ws.service.impl;


import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.cronjob.enums.ErrorMode;
import de.hybris.platform.cronjob.model.JobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.i18n.daos.CountryDao;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.solgroup.core.common.daos.CommonDao;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.job.BatchImportCronJobModel;
import com.solgroup.core.ws.service.CommonWsService;


/**
 * @author marcodanielecoppola
 *
 */
public class DefaultCommonWsService implements CommonWsService
{
	private static final Logger LOG = Logger.getLogger(DefaultCommonWsService.class);
	private static final String MEDIA_FOLDER = "WSImport";
	private static final String COMMON_CATALOG_VERSION = "Online";
	private static final String PREFIX_IMPORT_JOB_PERFORMABLE = "app.core.services.batchimport.perform.auto.";
	private static final String PREFIX_IMPORT_JOB_SYNCHRONOUS = "app.core.services.batchimport.perform.synchronous.";
	private static final String sourceSystemSeparator = "_";


	private ModelService modelService;
	private MediaService mediaService;
	private CatalogVersionService catalogVersionService;
	private CronJobService cronJobService;
	private UserService userService;
	private CommonDao commonDao;
	private ConfigurationService configurationService;
	private CountryDao countryDao;

	@Override
	public void prepareImportDataJob(final Object dataToImport, final String jobName) throws Exception
	{
		final byte[] bytesXML;
		if(jobName.equals("importStockAfterOrderJob") || jobName.equals("importStockJob")) {
			bytesXML = xmlToByteArray(dataToImport, "com.solgroup.core.ws.services.stock", "Root");
		}
		else {
			bytesXML = xmlToByteArray(dataToImport);
		}
		
		final String sourceSystem = getSourceSystem(dataToImport, jobName);
		final String countryIsoCode = getCountry(dataToImport, sourceSystem, jobName);
		final String fileName = generateFileName(dataToImport, sourceSystem, jobName);
		final MediaModel xmlMedia = writeMedia(bytesXML, fileName, sourceSystem, jobName);
		createBatchImportCronJobModel(xmlMedia, jobName, sourceSystem, countryIsoCode);
	}


	public CMSSiteModel getCMSSite(final String legacySystemCode, final String countryIsoCode)
	{

		final LegacySystemModel legacySystem = getCommonDao().findItemByUniqueCode(LegacySystemModel._TYPECODE,
				LegacySystemModel.CODE, legacySystemCode, LegacySystemModel.class);
		CountryModel country = null;
		final Collection<CountryModel> countries = getCountryDao().findCountriesByCode(countryIsoCode);
		if (CollectionUtils.isNotEmpty(countries))
		{
			country = countries.iterator().next();
		}

		if (legacySystem == null || country == null)
		{
			return null;
		}

		final Set<CMSSiteModel> cmsSites_fromLegacy = legacySystem.getCmsSites();
		final CMSSiteModel cmsSite_fromCountry = country.getCmsSite();
		if (CollectionUtils.isNotEmpty(cmsSites_fromLegacy) && cmsSite_fromCountry != null)
		{
			for (final CMSSiteModel cmsSite : cmsSites_fromLegacy)
			{
				if (cmsSite.equals(cmsSite_fromCountry))
				{
					return cmsSite;
				}
			}
		}

		return null;

	}


	public Object parseXmlStream(final Class objectClass, final InputStream stream)
	{
		try
		{
			final JAXBContext jaxbContext = JAXBContext.newInstance(objectClass);
			final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			return jaxbUnmarshaller.unmarshal(stream);
		}
		catch (final JAXBException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	
	
	public Object parseXmlStream_withoutRootNode(final Class objectClass, final InputStream stream)
	{
		try
		{
			final JAXBContext jaxbContext = JAXBContext.newInstance(objectClass);
			final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			JAXBElement root = jaxbUnmarshaller.unmarshal(new StreamSource(stream),objectClass);
			return root.getValue();
			
		}
		catch (final JAXBException e)
		{
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Convert xml object in byte array
	 *
	 * @param obj
	 * @return
	 * @throws JAXBException
	 * @throws IOException
	 */
	private byte[] xmlToByteArray(final Object obj) throws JAXBException, IOException
	{

		final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		final JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
		final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		
		jaxbMarshaller.marshal(obj, byteArray);
		LOG.debug("XML generated");
		return byteArray.toByteArray();
	}

	
	private byte[] xmlToByteArray(final Object obj, String rootPackage, String rootNode) throws JAXBException, IOException
	{

		final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		final JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
		final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

 	    QName qName = new QName(rootPackage, rootNode);
 	    JAXBElement root = new JAXBElement(qName, obj.getClass(), obj);

		
		jaxbMarshaller.marshal(root, byteArray);
		LOG.debug("XML generated");
		return byteArray.toByteArray();
		
		
	}

	
	
	/**
	 * Get sourceSystem from xml object
	 *
	 * @param dataToImport
	 * @param jobName
	 * @return
	 */
	public String getSourceSystem(final Object dataToImport, final String jobName)
	{
		String sourceSystem = "";
		try
		{
			final Object header = dataToImport.getClass().getMethod("getHeader", null).invoke(dataToImport, null);
			final Object sourceSystemHeader = header.getClass().getMethod("getSourceSystem", null).invoke(header, null);
			if (sourceSystemHeader instanceof String)
			{
				sourceSystem = sourceSystemHeader.toString();
			}
			else
			{
				LOG.error("No source system found during import for " + jobName);
			}
		}
		catch (final Exception exc)
		{
			LOG.error(exc.getMessage(), exc);
		}

		return sourceSystem;

	}


	/**
	 * Get country from xml object
	 *
	 * @param dataToImport
	 * @param jobName
	 * @return
	 */

	public String getCountry(final Object dataToImport, final String sourceSystem, final String jobName)
	{
		//TODO rimuovere la merda qui sotto
		String countryIsoCode = "IT";
		//String countryIsoCode = "";
		try
		{

			final Object country = dataToImport.getClass().getMethod("getCountry", null).invoke(dataToImport, null);

			// Retrieve country from xml (country tag)
			if (country != null && country instanceof String)
			{
				countryIsoCode = country.toString();
			}
			// Retrive country from source system
			else if (sourceSystem.indexOf("_") > 0)
			{
				final String tokens[] = sourceSystem.split(sourceSystemSeparator);
				if (tokens != null && tokens.length >= 2)
				{
					countryIsoCode = tokens[1];
				}
			}
		}
		catch (

		final Exception exc)

		{
			LOG.error(exc.getMessage(), exc);
		}

		return countryIsoCode;

	}


	/**
	 * Generate Media name
	 *
	 * @param dataToImport
	 * @param jobName
	 * @return
	 */
	public String generateFileName(final Object dataToImport, final String jobName, final String sourceSystem)
	{
		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS");

		return new StringBuilder(jobName).append("_").append(sourceSystem).append("_").append(dateFormat.format(new Date()))
				.append(".xml").toString();
	}

	/**
	 * Write media as itemModel
	 *
	 * @param byteArray
	 * @param filename
	 * @param legacySystemCode
	 * @return
	 * @throws Exception
	 */
	private MediaModel writeMedia(final byte[] byteArray, final String filename, final String legacySystemCode, String jobName) throws Exception
	{

		final MediaModel media = modelService.create(MediaModel.class);
		final MediaFolderModel mediaFolder = mediaService.getFolder(MEDIA_FOLDER + "/" + jobName + "/" + legacySystemCode);
		final String code = new StringBuilder("import:" + filename).toString();
		final LegacySystemModel legacySystem = getCommonDao().findItemByUniqueCode(LegacySystemModel._TYPECODE,
				LegacySystemModel.CODE, legacySystemCode, LegacySystemModel.class);
		if (legacySystem == null)
		{
			throw new Exception("Legacy system is null for " + legacySystemCode);
		}
		final CatalogModel commonCatalog = legacySystem.getCommonCatalog();
		media.setCode(code);
		media.setRealFileName(filename);
		media.setDescription(filename);
		media.setFolder(mediaFolder);
		media.setCatalogVersion(getCatalogVersionService().getCatalogVersion(commonCatalog.getId(), COMMON_CATALOG_VERSION));
		modelService.save(media);
		mediaService.setStreamForMedia(media, new ByteArrayInputStream(byteArray));

		LOG.debug("Media created: " + code);
		return media;
	}

	private void createBatchImportCronJobModel(final MediaModel media, final String cronJobName, final String sourceSystem,
			final String countryIsoCode)
	{

		// Find cmsSite
		final CMSSiteModel cmsSite = getCMSSite(sourceSystem, countryIsoCode);
		if (cmsSite == null)
		{
			LOG.error(String.format(
					"CMS site is null for legacySystem %s and country %s. It is not possible to create improt cronJob %s",
					sourceSystem, countryIsoCode, cronJobName));
		}

		// create job
		final JobModel myJobModel = cronJobService.getJob(cronJobName);
		final BatchImportCronJobModel myCronJob = modelService.create(BatchImportCronJobModel.class);
		myCronJob.setSite(cmsSite);
		myCronJob.setJob(myJobModel);
		myCronJob.setCode(getJobCode(cronJobName));
		myCronJob.setImportMedia(media);
		myCronJob.setSessionUser(getImportUser());

		myCronJob.setLegacySystem(getCommonDao().findItemByUniqueCode(LegacySystemModel._TYPECODE, LegacySystemModel.CODE,
				sourceSystem, LegacySystemModel.class));
		// Find country
		final Collection<CountryModel> countries = getCountryDao().findCountriesByCode(countryIsoCode);
		if (CollectionUtils.isNotEmpty(countries))
		{
			final CountryModel country = countries.iterator().next();
			myCronJob.setCountry(country);
		}
		
		if(cronJobName.equals("importProductJob") || cronJobName.equals("importCustomerJob")) {
			myCronJob.setExecutionOrder(new Integer(0));
		}
		else if(cronJobName.equals("importPriceListsJob") || cronJobName.equals("importStockJob")) {
			myCronJob.setExecutionOrder(new Integer(1));
		}
		else {
			myCronJob.setExecutionOrder(new Integer(-1));
		}
		
		myCronJob.setErrorMode(ErrorMode.FAIL);
		myCronJob.setRemoveOnExit(Boolean.TRUE);


		// myCronJob.setSessionLanguage();
		// myCronJob.setSessionCurrency();
		modelService.save(myCronJob);
		LOG.info("Job created: " + myCronJob.getCode());
		try
		{
			if (getConfigurationService().getConfiguration().getBoolean(PREFIX_IMPORT_JOB_PERFORMABLE + cronJobName))
			{
				boolean synchronous = getConfigurationService().getConfiguration().getBoolean(PREFIX_IMPORT_JOB_SYNCHRONOUS + cronJobName, false);
				LOG.info("Starting Job: " + myCronJob.getCode()); // starting job
				cronJobService.performCronJob(myCronJob,synchronous);
			}
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
	}

	private UserModel getImportUser()
	{
		return userService.getAdminUser();
	}

	private String getJobCode(final String jobName)
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");

		return new StringBuilder(jobName).append("-IMPORT-").append(sdf.format(cal.getTime())).toString();
	}




	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	protected CronJobService getCronJobService()
	{
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected CommonDao getCommonDao()
	{
		return commonDao;
	}

	@Required
	public void setCommonDao(final CommonDao commonDao)
	{
		this.commonDao = commonDao;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}


	protected CountryDao getCountryDao()
	{
		return countryDao;
	}


	@Required
	public void setCountryDao(final CountryDao countryDao)
	{
		this.countryDao = countryDao;
	}


	@Override
	public CountryModel getCountryForLegacySystemAndSite(LegacySystemModel legacySystem, CMSSiteModel site) {
		
		if(!legacySystem.getCmsSites().contains(site))
		{
			LOG.error("Immpossible retrieve country for legacy system " + legacySystem.getCode() + " ("+legacySystem.getLegacyName()+") and site "+site.getName());
			return null;
		}
			

		return site.getCountries().iterator().next();
	}





}
