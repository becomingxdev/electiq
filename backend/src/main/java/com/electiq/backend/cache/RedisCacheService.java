package com.electiq.backend.cache;

import com.electiq.backend.config.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder implementation of {@link CacheService} for Google Cloud Memorystore (Redis).
 *
 * <p><strong>Production roadmap:</strong> When the application is deployed on Cloud Run
 * with a Memorystore Redis instance, replace this stub with a Jedis or
 * Spring Data Redis implementation. All cache reads and writes are currently
 * no-ops, meaning every request falls through to the underlying service layer.
 *
 * <p>This class is intentionally <em>not</em> annotated with {@code @Service} so that
 * {@link com.electiq.backend.config.CacheConfig} remains the single point of bean
 * instantiation for the entire cache subsystem.
 */
public class RedisCacheService implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);

    /**
     * {@inheritDoc}
     * <p>Planned: retrieve value from Redis using the given key, honouring a
     * per-key TTL of {@value AppConstants#CACHE_TTL_MS} ms.
     */
    @Override
    public String get(String key) {
        // TODO: Integrate with Spring Data Redis / Jedis
        logger.debug("Redis GET (stub) — key: {}", key);
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>Planned: persist the value in Redis with EX (expire) option.
     */
    @Override
    public void set(String key, String value) {
        // TODO: Implement distributed SET with TTL
        logger.debug("Redis SET (stub) — key: {}", key);
    }

    /**
     * {@inheritDoc}
     * <p>Planned: use Redis EXISTS command.
     */
    @Override
    public boolean contains(String key) {
        // TODO: Implement Redis EXISTS check
        return false;
    }
}
