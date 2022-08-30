package com.solgroup.job.importData.scheduler;

import com.google.common.collect.Sets;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.job.BatchImportCronJobModel;
import com.solgroup.core.model.job.SchedulerImportCronJobModel;
import com.solgroup.daos.SolGroupSchedulerDao;
import com.solgroup.job.importData.abstractJob.AbstractImportJob;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class SchedulerImportJob extends AbstractImportJob {
	
	private static final Logger LOG = Logger.getLogger(SchedulerImportJob.class);
	
	private SolGroupSchedulerDao solGroupSchedulerDao;
	private CronJobService cronJobService;

	@Override
	public PerformResult perform(CronJobModel cronJob) {

		if(cronJob instanceof SchedulerImportCronJobModel) {
			
			SchedulerImportCronJobModel schedulerImportCronJob = (SchedulerImportCronJobModel)cronJob;
			CMSSiteModel site = schedulerImportCronJob.getSite();
			LegacySystemModel legacySystem = schedulerImportCronJob.getLegacySystem();
			
			if(site==null) {
				LOG.error("No site defined for scheduler import cronJob " + schedulerImportCronJob.getCode());
				PerformResult performResult = new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
				return performResult;
			}
			
			if(legacySystem==null) {
				LOG.error("No legacy system defined for scheduler import cronJob " + schedulerImportCronJob.getCode());
				PerformResult performResult = new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
				return performResult;
			}
			
			List<BatchImportCronJobModel> performableCronJobs = getSolGroupSchedulerDao().findImportableCronJobs(site, legacySystem);
			
			if(CollectionUtils.isNotEmpty(performableCronJobs)) {
				
				
				LOG.info("Found " + performableCronJobs.size() + " performable batchImportCronJobs");
				int lowerExecutionOrder = -1;
				Set<String> jobsNameQueue = Sets.newHashSet();
				Set<BatchImportCronJobModel> queue = Sets.newHashSet();
				for(BatchImportCronJobModel batchImportCronJob : performableCronJobs) {
					if ((new Date().getTime() - cronJob.getCreationtime().getTime()) < 10800000) //logs only for 3 hours to reduce log file dimensions
						LOG.info(String.format("Check batchImportCronJob : code [%s], job [%s], priority [%s]", batchImportCronJob.getCode(), batchImportCronJob.getJob().getCode(), batchImportCronJob.getExecutionOrder().intValue()));
					if(lowerExecutionOrder<0 || jobsNameQueue.isEmpty()) {
						lowerExecutionOrder = batchImportCronJob.getExecutionOrder();
						addJobInQueue(queue, jobsNameQueue, batchImportCronJob);
					}
					else if(lowerExecutionOrder==batchImportCronJob.getExecutionOrder().intValue()) {
						if(!jobsNameQueue.contains(batchImportCronJob.getJob().getCode())) {
							addJobInQueue(queue, jobsNameQueue, batchImportCronJob);
						}
					}
					else {
						LOG.info("Break loop");
						break;
					}
				}
				
				
				if(CollectionUtils.isNotEmpty(queue)) {
					for(BatchImportCronJobModel batchImportCronJob : queue) {
						getCronJobService().performCronJob(batchImportCronJob,true);
					}
					
				}
			}
			
			
		}
		
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}
	
	
	private void addJobInQueue(Set<BatchImportCronJobModel> queue, Set<String> jobsNameQueue, BatchImportCronJobModel cronJob) {
		
		if(
				(cronJob.getResult()!=null && 
					(cronJob.getResult().equals(CronJobResult.ERROR) ||
					 cronJob.getResult().equals(CronJobResult.FAILURE) ))
				||
				(cronJob.getStatus()!=null && 
					(cronJob.getStatus().equals(CronJobStatus.ABORTED) ||
					 cronJob.getStatus().equals(CronJobStatus.RUNNING) ||
				     cronJob.getStatus().equals(CronJobStatus.PAUSED) )) 
				)
		{

			if ((new Date().getTime() - cronJob.getCreationtime().getTime()) < 10800000) //logs only for 3 hours to reduce log file dimensions
					LOG.info(String.format("BatchImportCronJob with code [%s], job [%s], priority [%s] can not be executed. Status is [%s] result is [%s]", cronJob.getCode(), cronJob.getJob().getCode(), cronJob.getExecutionOrder().intValue(), cronJob.getStatus().getCode(), cronJob.getResult().getCode()));
			return;
		}

		jobsNameQueue.add(cronJob.getJob().getCode());
		queue.add(cronJob);
		LOG.info(String.format("Add batchImportCronJob in queue : code [%s], job [%s], priority [%s]", cronJob.getCode(), cronJob.getJob().getCode(), cronJob.getExecutionOrder().intValue()));
	}

	protected SolGroupSchedulerDao getSolGroupSchedulerDao() {
		return solGroupSchedulerDao;
	}

	@Required
	public void setSolGroupSchedulerDao(SolGroupSchedulerDao solGroupSchedulerDao) {
		this.solGroupSchedulerDao = solGroupSchedulerDao;
	}

	protected CronJobService getCronJobService() {
		return cronJobService;
	}

	@Required
	public void setCronJobService(CronJobService cronJobService) {
		this.cronJobService = cronJobService;
	}
	
	
	
	

}
