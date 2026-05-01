package com.electiq.backend.config;

import com.electiq.backend.cache.CacheService;
import com.electiq.backend.cache.InMemoryCacheService;
import com.electiq.backend.cache.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to select the CacheService implementation.
 * Controlled by the 'CACHE_TYPE' environment variable.
 */
@Configuration
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${CACHE_TYPE:memory}")
    private String cacheType;

    /**
     * Provides the appropriate CacheService bean based on configuration.
     * 
     * @return InMemoryCacheService if CACHE_TYPE is 'memory' or unset.
     *         RedisCacheService if CACHE_TYPE is 'redis'.
     */
    @Bean
    public CacheService cacheService() {
        if ("redis".equalsIgnoreCase(cacheType)) {
            logger.info("Using Redis Cache implementation (Placeholder)");
            return new RedisCacheService();
        } else {
            logger.info("Using InMemory Cache implementation");
            return new InMemoryCacheService();
        }
    }
}
