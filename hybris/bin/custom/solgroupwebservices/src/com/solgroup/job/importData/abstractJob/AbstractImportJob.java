
package com.solgroup.job.importData.abstractJob;

import java.io.ByteArrayInputStream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.common.daos.CommonDao;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.util.SolGroupUtilities;
import com.solgroup.core.ws.service.CommonWsService;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cronjob.enums.ErrorMode;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.impex.constants.ImpExConstants;
import de.hybris.platform.impex.jalo.ImpExManager;
import de.hybris.platform.impex.jalo.ImpExMedia;
import de.hybris.platform.impex.jalo.cronjob.ImpExImportCronJob;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.CSVConstants;


/**
 * @author salvo
 *
 */
public abstract class AbstractImportJob extends AbstractJobPerformable<CronJobModel>
{

	private CommonWsService commonWsService;
	private MediaService mediaService;
	private ModelService modelService;
	private CommonDao commonDao;
	private SolGroupUtilities solGroupUtilities;
	private UserService userService;
	private EnumerationService enumerationService;
	private CatalogVersionService catalogVersionService;
	private ConfigurationService configurationService;
	

	protected CommonWsService getCommonWsService()
	{
		return commonWsService;
	}



	@Required
	public void setCommonWsService(final CommonWsService commonWsService)
	{
		this.commonWsService = commonWsService;
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Override
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected SolGroupUtilities getSolGroupUtilities()
	{
		return solGroupUtilities;
	}

	@Required
	public void setSolGroupUtilities(final SolGroupUtilities solGroupUtilities)
	{
		this.solGroupUtilities = solGroupUtilities;
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

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
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

	protected EnumerationService getEnumerationService()
	{
		return enumerationService;
	}


	@Required
	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}
	
	

//    protected void createImpex(String builder) throws Exception {
//        if (builder != null && !builder.toString().isEmpty()) {
//
//            ByteArrayInputStream bais = new ByteArrayInputStream(builder.toString().getBytes());
//
//            final ImpExMedia impexMedia = ImpExManager.getInstance().createImpExMedia("generated impex media",
//                    CSVConstants.HYBRIS_ENCODING);
//            impexMedia.setFieldSeparator(CSVConstants.HYBRIS_FIELD_SEPARATOR);
//            impexMedia.setQuoteCharacter(CSVConstants.HYBRIS_QUOTE_CHARACTER);
//            impexMedia.setData(bais, impexMedia.getCode() + "." + ImpExConstants.File.EXTENSION_CSV,
//                    ImpExConstants.File.MIME_TYPE_CSV);
//
//            ImpExImportCronJob impexMediaImportCronJob = ImpExManager.getInstance().createDefaultImpExImportCronJob();
//
//            impexMediaImportCronJob.setEnableCodeExecution(true);
//            impexMediaImportCronJob.setLogToDatabase(false);
//            impexMediaImportCronJob.setJobMedia(impexMedia);
//            impexMediaImportCronJob.setMaxThreads(1);
//            impexMediaImportCronJob.setRemoveOnExit(false);
//
//            // always remove if end with success
//            boolean removeOnSuccess = true;
//            boolean synchronous = true;
//            impexMediaImportCronJob = ImpExManager.getInstance().importData(impexMediaImportCronJob, synchronous,
//                    removeOnSuccess);
//            
//        }
//
//    }


	protected ConfigurationService getConfigurationService() {
		return configurationService;
	}

	@Required
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}



	public void performImpexImport(String impexToImport, CMSSiteModel site, String sourceSystem, String messageId, String jobCode) throws Exception {
		
		String myMessageId = "messageId";
		if(StringUtils.isNotEmpty(messageId)) {
			myMessageId = messageId;
		}
		
		String impexCronJobCode = String.format(SolgroupCoreConstants.IMPEX_IMPORT_CRONJOB_CODE_PATTERN, site.getUid(),sourceSystem,jobCode + "_job",myMessageId,System.currentTimeMillis());
		String impexMediaCode = String.format(SolgroupCoreConstants.IMPEX_IMPORT_CRONJOB_CODE_PATTERN, site.getUid(),sourceSystem,jobCode + "_media",myMessageId,System.currentTimeMillis()); 
		getSolGroupUtilities().importImpex(impexToImport, impexMediaCode, impexCronJobCode, getConfigurationService().getConfiguration().getInt("app.core.services.batchimport.threads." + jobCode,1), true);

		
	}

    

}
