package com.electiq.backend.cache;

import com.electiq.backend.config.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link CacheService} backed by a {@link ConcurrentHashMap}.
 *
 * <p>Uses a <em>lazy expiry</em> strategy: stale entries are detected and evicted
 * during {@link #get(String)} calls rather than via a background sweep. This
 * eliminates the need for a dedicated cleanup thread and keeps the implementation
 * simple and cloud-friendly.
 *
 * <p>TTL is controlled by {@link AppConstants#CACHE_TTL_MS} (default: 5 minutes).
 */
public class InMemoryCacheService implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCacheService.class);

    /** Thread-safe backing store: key → (value, insertion timestamp). */
    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // CacheService implementation
    // -------------------------------------------------------------------------

    /**
     * Retrieves the value for the given key if it exists and has not expired.
     *
     * @param key the cache key; must not be {@code null}
     * @return the cached value, or {@code null} if absent or expired
     */
    @Override
    public String get(String key) {
        if (key == null) return null;

        CacheEntry entry = store.get(key);
        if (entry == null) {
            logger.debug("Cache MISS for key: {}", key);
            return null;
        }

        if (entry.isExpired()) {
            logger.debug("Cache EXPIRED for key: {}", key);
            store.remove(key);
            return null;
        }

        logger.debug("Cache HIT for key: {}", key);
        return entry.value;
    }

    /**
     * Stores a key-value pair in the cache with a fresh TTL timestamp.
     * Null keys or values are silently ignored.
     *
     * @param key   the cache key
     * @param value the value to cache
     */
    @Override
    public void set(String key, String value) {
        if (key == null || value == null) return;
        logger.debug("Cache SET for key: {}", key);
        store.put(key, new CacheEntry(value));
    }

    /**
     * Returns {@code true} if the cache contains a non-expired entry for the key.
     *
     * @param key the cache key
     * @return {@code true} if a live entry is present
     */
    @Override
    public boolean contains(String key) {
        if (key == null) return false;
        CacheEntry entry = store.get(key);
        if (entry == null) return false;
        if (entry.isExpired()) {
            store.remove(key);
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Internal value wrapper
    // -------------------------------------------------------------------------

    /** Immutable pair of (cached value, insertion timestamp). */
    private static final class CacheEntry {
        final String value;
        final long createdAt;

        CacheEntry(String value) {
            this.value     = value;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - createdAt) > AppConstants.CACHE_TTL_MS;
        }
    }
}
