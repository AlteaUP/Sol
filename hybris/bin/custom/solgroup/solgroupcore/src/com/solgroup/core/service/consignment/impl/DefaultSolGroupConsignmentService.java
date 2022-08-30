package com.solgroup.core.service.consignment.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.dao.consignment.SolGroupConsignmentDao;
import com.solgroup.core.service.consignment.SolGroupConsignmentService;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.site.BaseSiteService;
import jersey.repackaged.com.google.common.collect.Sets;

/**
 * @author fmilazzo
 *
 */
public class DefaultSolGroupConsignmentService implements SolGroupConsignmentService {

    private SolGroupConsignmentDao solGroupConsignmentDao;
    private BaseSiteService baseSiteService;

    @Override
    public ConsignmentModel getConsignmentForCode(final String consignmentCode, final String orderCode) {
        final BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        return getSolGroupConsignmentDao().findConsignmentByCode(orderCode, consignmentCode, baseSite);
    }
    

	@Override
	public ConsignmentEntryModel getConsignmentEntryByProductCode(final ConsignmentModel consignment, final String productCode) {
		final List<ConsignmentEntryModel> consignmentEntries = getSolGroupConsignmentDao().findConsignmentEntriesByProductCode(consignment, productCode);
		if(CollectionUtils.isNotEmpty(consignmentEntries))
            return consignmentEntries.get(0);
		return null;
	}

	@Override
	public boolean isFinalStatus(final String consignmentStatus) {
		
		final Set<String> notFinalStatus = Sets.newHashSet();
		final Set<String> finalStatus = Sets.newHashSet();
		
		notFinalStatus.add(ConsignmentStatus.ACCEPTED.getCode());
		notFinalStatus.add(ConsignmentStatus.PLANNED.getCode());
		notFinalStatus.add(ConsignmentStatus.SHIPPED.getCode());
		
		finalStatus.add(ConsignmentStatus.DELIVERED.getCode());
		finalStatus.add(ConsignmentStatus.CANCELLED.getCode());
		finalStatus.add(ConsignmentStatus.DELETED.getCode());
		
		if(notFinalStatus.contains(consignmentStatus))
            return false;
        else if(finalStatus.contains(consignmentStatus))
            return true;
		
		return false;
	}

    @Override
    public ConsignmentModel getConsignmentForCode(final String consignmentCode, final String orderCode, final BaseSiteModel baseSite) {
        return getSolGroupConsignmentDao().findConsignmentByCode(orderCode, consignmentCode, baseSite);
    }

	
    protected SolGroupConsignmentDao getSolGroupConsignmentDao() {
        return solGroupConsignmentDao;
    }

    @Required
    public void setSolGroupConsignmentDao(final SolGroupConsignmentDao solGroupConsignmentDao) {
        this.solGroupConsignmentDao = solGroupConsignmentDao;
    }

    protected BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    @Required
    public void setBaseSiteService(final BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

}
