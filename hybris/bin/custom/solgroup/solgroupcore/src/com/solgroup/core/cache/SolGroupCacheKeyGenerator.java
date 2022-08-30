/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2013 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package com.solgroup.core.cache;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import jersey.repackaged.com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;


/**
 * @author salvo
 * 
 */
public class SolGroupCacheKeyGenerator implements KeyGenerator {

    private static final Logger LOG = Logger.getLogger(SolGroupCacheKeyGenerator.class);

    
    @Autowired
    private CMSSiteService cmsSiteService;


    @Override
    public Object generate(final Object target, final Method method, final Object... params) {
    	List<Object> key = Lists.newArrayList();
    	addSite(key);
    	addParams(key, params);
    	return key;
    }
    

    protected void addSite(final List<Object> key) {
    	CMSSiteModel currentSite = cmsSiteService.getCurrentSite();
    	if(currentSite!=null) {
    		key.add(currentSite.getUid());
    	}
    }
    
    
    
    protected void addParams(final List<Object> key, final Object... params) {
        for (final Object o : params) {
            key.add(o);
        }
    }

}