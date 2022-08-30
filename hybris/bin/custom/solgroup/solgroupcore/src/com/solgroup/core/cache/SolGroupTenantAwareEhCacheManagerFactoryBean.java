package com.solgroup.core.cache;

import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import de.hybris.platform.core.Registry;
import net.sf.ehcache.CacheException;

public class SolGroupTenantAwareEhCacheManagerFactoryBean extends EhCacheManagerFactoryBean {

	
    private String cacheNamePrefix = "solGroupCache_";

    @Override
    public void afterPropertiesSet() throws CacheException {
        setCacheManagerName(cacheNamePrefix + Registry.getCurrentTenant().getTenantID());
        super.afterPropertiesSet();
    }

    public void setCacheNamePrefix(final String cacheNamePrefix) {
        this.cacheNamePrefix = cacheNamePrefix;
    }

}
