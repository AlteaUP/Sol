package com.solgroup.facades.search.context.listeners;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.solgroup.core.processors.QueryPostProcessor;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.Document;
import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;

public class CustomPriceListFacetSearchListener implements FacetSearchListener{
	
	private static final String PRICE_VALUE = "priceValue";


    private final Logger logger = Logger.getLogger(QueryPostProcessor.class);

    @Resource(name="commonI18NService")
    private CommonI18NService commonI18NService;

    @Resource(name="sessionService")
    private SessionService sessionService;
    
    @Resource(name="userService")
    private UserService userService;


	@Override
	public void beforeSearch(FacetSearchContext arg0) throws FacetSearchException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterSearch(FacetSearchContext facetSearchContext) throws FacetSearchException {
		SearchQuery searchQuery = facetSearchContext.getSearchQuery();
		
		
		final String fieldName = PRICE_VALUE;
		final String priceListCode = sessionService.getAttribute("priceListCode");

		addPriceListFilter(searchQuery, fieldName, priceListCode);
		
		
		
		
		
		
	}

	@Override
	public void afterSearchError(FacetSearchContext arg0) throws FacetSearchException {
		// TODO Auto-generated method stub
		
	}
	
	 private void addPriceListFilter(final SearchQuery searchQuery, String fieldName, String priceListCode) {

	        // TODO aggiungere gestione per tipo ordine diverso da quello riassortimento
		 
		 	
			
			final IndexedProperty prop = searchQuery.getIndexedType().getIndexedProperties().get(fieldName);
			
			CurrencyModel sessionCurrencyModel = commonI18NService.getCurrentCurrency();
			
			String sessionCurrency = sessionCurrencyModel.getIsocode();
			
			String newFieldName = fieldName + "_" + sessionCurrency;
			
			if(priceListCode !=null &&!priceListCode.isEmpty()){
			
				newFieldName = getCustomPriceRangeFieldName(prop, fieldName, prop.getType(), null, priceListCode);
			
			}

	        final StringBuilder result = new StringBuilder();
	        result.append("(").append(newFieldName).append(":[ 0 TO * ])");

	        searchQuery.addFilterQuery(result.toString());
	        
	        

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
