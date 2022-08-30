package com.solgroup.job.importData.jobs;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.model.job.MassiveStockCronJobModel;
import com.solgroup.service.impl.DefaultSolGroupWSStockService;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

public class MassiveStockJob extends AbstractJobPerformable<CronJobModel> {

	private Logger LOG = Logger.getLogger(this.getClass());

	private DefaultSolGroupWSStockService solGroupWSStockService;

	@Override
	public PerformResult perform(CronJobModel cronJob) {
		if (cronJob instanceof MassiveStockCronJobModel) {

			MassiveStockCronJobModel massiveStockCronJob = (MassiveStockCronJobModel) cronJob;

			try {
				getSolGroupWSStockService().sendStockRequest(massiveStockCronJob.getLegacySystem(), massiveStockCronJob.getCountry());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);

		}
		return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);

	}

	protected DefaultSolGroupWSStockService getSolGroupWSStockService() {
		return solGroupWSStockService;
	}

	@Required
	public void setSolGroupWSStockService(DefaultSolGroupWSStockService solGroupWSStockService) {
		this.solGroupWSStockService = solGroupWSStockService;
	}


	
	
}
