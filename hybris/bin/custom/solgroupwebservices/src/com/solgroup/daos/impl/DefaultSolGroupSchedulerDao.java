package com.solgroup.daos.impl;

import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.job.BatchImportCronJobModel;
import com.solgroup.daos.SolGroupSchedulerDao;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import jersey.repackaged.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class DefaultSolGroupSchedulerDao extends AbstractItemDao implements SolGroupSchedulerDao {

	@Override
	public List<BatchImportCronJobModel> findImportableCronJobs(CMSSiteModel site, LegacySystemModel legacySystem) {

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {c." + ItemModel.PK + "} from {");
		queryBuilder.append(BatchImportCronJobModel._TYPECODE + " as c");
		queryBuilder.append(" join CronJobStatus as status on {status.pk}={c." + BatchImportCronJobModel.STATUS + "}");
		queryBuilder.append(" join CronJobResult as result on {result.pk}={c." + BatchImportCronJobModel.RESULT + "}}");
		queryBuilder.append(" where ");
		queryBuilder.append("{c." + BatchImportCronJobModel.SITE + "}=?site");
		queryBuilder.append(" AND {c." + BatchImportCronJobModel.LEGACYSYSTEM + "}=?legacySystem");
		queryBuilder.append(" AND ({status.code} NOT IN ('" + CronJobStatus.FINISHED + "')");
		queryBuilder.append(" OR {result.code} NOT IN ('" + CronJobResult.SUCCESS + "'))");
		queryBuilder.append(" AND {c." + BatchImportCronJobModel.ACTIVE + "} = 1");
		queryBuilder.append(" AND {c." + BatchImportCronJobModel.EXECUTIONORDER + "} >= 0");
		queryBuilder.append(" order by {c." + BatchImportCronJobModel.EXECUTIONORDER + "},{c." + BatchImportCronJobModel.CREATIONTIME + "} ");
		
		final Map<String, Object> params = Maps.newHashMap();
		params.put("site", site);
		params.put("legacySystem", legacySystem);
		
		final List<BatchImportCronJobModel> result = getFlexibleSearchService()
				.<BatchImportCronJobModel> search(queryBuilder.toString(), params).getResult();
		
		return result;

	}

}
