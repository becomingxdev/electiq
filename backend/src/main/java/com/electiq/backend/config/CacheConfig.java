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
 * Provides the active {@link CacheService} implementation as a Spring bean.
 *
 * <h2>Selection logic</h2>
 * <p>Set the {@code CACHE_TYPE} environment variable to one of:
 * <ul>
 *   <li>{@code memory} (default) — {@link InMemoryCacheService}, suitable for single-instance deployments.</li>
 *   <li>{@code redis} — {@link RedisCacheService} stub, intended for production multi-instance Cloud Run.</li>
 * </ul>
 *
 * <p>The selected implementation is logged at startup so operators can
 * immediately verify cache configuration without inspecting environment variables.
 */
@Configuration
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    private static final String CACHE_TYPE_REDIS  = "redis";
    private static final String CACHE_TYPE_MEMORY = "memory";

    @Value("${CACHE_TYPE:" + CACHE_TYPE_MEMORY + "}")
    private String cacheType;

    /**
     * Selects and exposes the appropriate {@link CacheService} implementation.
     *
     * @return configured cache service bean
     */
    @Bean
    public CacheService cacheService() {
        if (CACHE_TYPE_REDIS.equalsIgnoreCase(cacheType)) {
            logger.info("Cache strategy: Redis (Google Cloud Memorystore — placeholder active)");
            return new RedisCacheService();
        }

        logger.info("Cache strategy: InMemory (TTL={}ms)", AppConstants.CACHE_TTL_MS);
        return new InMemoryCacheService();
    }
}
