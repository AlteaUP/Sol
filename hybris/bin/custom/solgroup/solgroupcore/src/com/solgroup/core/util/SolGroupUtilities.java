/**
 *
 */
package com.solgroup.core.util;

import de.hybris.platform.category.CategoryService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.enums.ErrorMode;
import de.hybris.platform.impex.constants.ImpExConstants;
import de.hybris.platform.impex.jalo.ImpExManager;
import de.hybris.platform.impex.jalo.ImpExMedia;
import de.hybris.platform.impex.jalo.cronjob.ImpExImportCronJob;
import de.hybris.platform.util.CSVConstants;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;

import jersey.repackaged.com.google.common.collect.Maps;


/**
 * @author salvo
 *
 */
public class SolGroupUtilities
{

	private CategoryService categoryService;


	private final Logger log = Logger.getLogger(SolGroupUtilities.class);


	public <T> Collection<T> updateUnmodifiableCollection(final Collection<T> currentUnmodifiableCollection,
			final Collection<T> deltaCollection)
	{

		final Set<T> newCollection = Sets.newHashSet();

		if (CollectionUtils.isNotEmpty(currentUnmodifiableCollection))
		{
			newCollection.addAll(currentUnmodifiableCollection);
		}


		if (CollectionUtils.isNotEmpty(deltaCollection))
		{
			newCollection.addAll(deltaCollection);
		}

		return newCollection;
	}

	public Map<String, Locale> getLocalesMap()
	{

		final Map<String, Locale> localesMap = Maps.newHashMap();

		final Locale locales[] = Locale.getAvailableLocales();
		if (locales != null && locales.length > 0)
		{
			for (final Locale locale : locales)
			{
				localesMap.put(locale.getCountry(), locale);
			}
		}
		return localesMap;
	}
	
    public void importImpex(String impexBody, String impexMediaCode, String impexCronJobCode, int maxThreads, boolean synchronous) throws Exception {
        if (impexBody != null && !impexBody.isEmpty()) {

            ByteArrayInputStream bais = new ByteArrayInputStream(impexBody.getBytes());

            final ImpExMedia impexMedia = ImpExManager.getInstance().createImpExMedia(impexMediaCode,
                    CSVConstants.HYBRIS_ENCODING);
            impexMedia.setFieldSeparator(CSVConstants.HYBRIS_FIELD_SEPARATOR);
            impexMedia.setQuoteCharacter(CSVConstants.HYBRIS_QUOTE_CHARACTER);
            impexMedia.setData(bais, impexMedia.getCode() + "." + ImpExConstants.File.EXTENSION_CSV,
                    ImpExConstants.File.MIME_TYPE_CSV);

            ImpExImportCronJob impexMediaImportCronJob = ImpExManager.getInstance().createDefaultImpExImportCronJob();

            impexMediaImportCronJob.setEnableCodeExecution(true);
            impexMediaImportCronJob.setLogToDatabase(false);
            impexMediaImportCronJob.setJobMedia(impexMedia);
            impexMediaImportCronJob.setMaxThreads(maxThreads);
            impexMediaImportCronJob.setRemoveOnExit(false);
            impexMediaImportCronJob.setCode(impexCronJobCode);
            
            ImpExImportCronJob resultCronJob =  ImpExManager.getInstance().importData(impexMediaImportCronJob, synchronous,false);

            // remove if end with success
            if(resultCronJob.getResult().getCode().equals(CronJobResult.SUCCESS.getCode()) && resultCronJob.getStatus().getCode().equals(CronJobStatus.FINISHED.getCode())) {
            	impexMedia.remove();
            	resultCronJob.remove();
            }
            else {
            	throw new Exception("CronJob " + resultCronJob.getCode() + " completed with errors !!!");
            }
            
            
            
//            if(!removeAlways) {
//                if(resultCronJob==null && impexMedia!=null) {
//                	impexMedia.remove();
//                }
//            }
//            // Remove always == true ==> remove importCronJob and media in both cases (completed with success and not)
//            else {
//            	if(resultCronJob!=null) {
//            		resultCronJob.remove();
//            	}
//            	if(impexMedia!=null) {
//            		impexMedia.remove();
//            	}
//            }
            
//            if(resultCronJob!=null && resultCronJob.getSuccessResult()!=null && resultCronJob.getResult()!=null && !resultCronJob.getSuccessResult().equals(resultCronJob.getResult())) {
//            	
//            }
            
        }

    }
	


	protected CategoryService getCategoryService()
	{
		return categoryService;
	}


	@Required
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}





}
