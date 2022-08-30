package com.solgroup.core.dao.tax;

import java.util.List;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.europe1.model.TaxRowModel;

public interface SolGroupTaxRowDao {

    /**
     * @param code
     * @return
     */
    List<TaxRowModel> findTaxRowsByCode(final String code, final CatalogVersionModel catalogVersion);

}
