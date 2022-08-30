package com.solgroup.core.solrfacetsearch.provider.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.StringUtils;

/**
 * @author fmilazzo
 *
 */
public class SolGroupSolrPriceRange {

    public static final String MAIN_SEPARATOR = "-";
    public static final String SEPARATOR = ":";

    public static String buildSolrPropertyFromPriceRows(final double minPrice, final String minUnit,
            final String minCurrency, final double maxPrice, final String maxUnit, final String maxCurrency) {
        return minPrice + SEPARATOR + minUnit + SEPARATOR + minCurrency + MAIN_SEPARATOR + maxPrice + SEPARATOR
                + maxUnit + SEPARATOR + maxCurrency;
    }

    public static List<String[]> splitSolrPropertyFromPriceRows(final String price) {
        List<String[]> result = null;
        if (!StringUtils.isEmpty(price)) {
            result = new ArrayList<String[]>();
            String[] fields = price.split(MAIN_SEPARATOR);
            for (int i = 0; i < fields.length; i++) {
                result.add(fields[i].split(SEPARATOR));
            }
        }
        return result;
    }
}
