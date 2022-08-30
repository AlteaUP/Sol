package com.solgroup.daos;

import java.util.List;

import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.job.BatchImportCronJobModel;

import de.hybris.platform.cms2.model.site.CMSSiteModel;

public interface SolGroupSchedulerDao {
	
	List<BatchImportCronJobModel> findImportableCronJobs(CMSSiteModel site, LegacySystemModel legacySystem);

}
