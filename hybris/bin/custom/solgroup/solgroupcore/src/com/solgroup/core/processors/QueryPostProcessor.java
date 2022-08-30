/**
 *
 */
package com.solgroup.core.processors;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.util.CollectionUtils;

import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SolrQueryPostProcessor;

/**
 * @author claudio
 *
 */
public class QueryPostProcessor implements SolrQueryPostProcessor {


    private static final String PRICE_VALUE = "priceValue";


    private final Logger logger = Logger.getLogger(QueryPostProcessor.class);

    @Resource(name="commonI18NService")
    private CommonI18NService commonI18NService;

    @Resource(name="sessionService")
    private SessionService sessionService;

    @Override
    public SolrQuery process(final SolrQuery paramSolrQuery, final SearchQuery paramSearchQuery) {

        addPriceListFilter(paramSolrQuery, paramSearchQuery);

        return paramSolrQuery;
    }

    private void addPriceListFilter(final SolrQuery paramSolrQuery, final SearchQuery paramSearchQuery) {

        final String fieldName = PRICE_VALUE;
        final IndexedProperty prop = paramSearchQuery.getIndexedType().getIndexedProperties().get(fieldName);

        final String priceListCode = sessionService.getAttribute("priceListCode");

        final String newFieldName = getCustomPriceRangeFieldName(prop, fieldName, prop.getType(), null, priceListCode);

        final StringBuilder result = new StringBuilder();
        result.append("(").append(newFieldName).append(":[ 0 TO * ])");

        paramSolrQuery.addFilterQuery(result.toString());

    }

    private String getCustomPriceRangeFieldName(final IndexedProperty indexedProperty, final String name, String type,
            String specifier, final String priceListCode) {

        final String separator = "_";

        if (isRanged(indexedProperty)) {
            type = "string";
        }
        type = type.toLowerCase();

        final StringBuilder fieldName = new StringBuilder();
        if (specifier == null) {
            fieldName.append(name).append(separator).append(separator).append(priceListCode).append(separator).append(type);
        } else {
            specifier = specifier.toLowerCase();
            if (type.equals("text")) {
                fieldName.append(name).append(separator).append(priceListCode).append(separator).append("text")
                        .append(separator).append(specifier.toLowerCase());
            } else {
                fieldName.append(name).append(separator).append(priceListCode).append(separator)
                        .append(specifier.toLowerCase()).append(separator).append(type);
            }
        }
        if (indexedProperty.isMultiValue()) {
            fieldName.append(separator).append("mv");
        }
        return fieldName.toString();
    }

    protected boolean isRanged(final IndexedProperty property) {
        return (!(CollectionUtils.isEmpty(property.getValueRangeSets())));
    }


    public CommonI18NService getCommonI18NService() {
        return commonI18NService;
    }

    public void setCommonI18NService(final CommonI18NService commonI18NService) {
        this.commonI18NService = commonI18NService;
    }

}
