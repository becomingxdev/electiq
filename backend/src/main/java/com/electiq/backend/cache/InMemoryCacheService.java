package com.electiq.backend.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of CacheService using ConcurrentHashMap.
 * Includes basic Time-To-Live (TTL) support to ensure data freshness.
 */
public class InMemoryCacheService implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCacheService.class);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    // Default TTL: 5 minutes
    private static final long TTL_MS = 5 * 60 * 1000;

    /**
     * Internal class to hold cached value and its creation timestamp.
     */
    private static class CacheEntry {
        final String value;
        final long timestamp;

        CacheEntry(String value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TTL_MS;
        }
    }

    @Override
    public String get(String key) {
        CacheEntry entry = cache.get(key);
        
        if (entry != null) {
            if (entry.isExpired()) {
                logger.info("Cache EXPIRED for key: {}", key);
                cache.remove(key);
                return null;
            }
            logger.info("Cache HIT for key: {}", key);
            return entry.value;
        }
        
        logger.info("Cache MISS for key: {}", key);
        return null;
    }

    @Override
    public void set(String key, String value) {
        if (key == null || value == null) return;
        
        logger.info("Cache SET for key: {}", key);
        cache.put(key, new CacheEntry(value));
    }

    @Override
    public boolean contains(String key) {
        CacheEntry entry = cache.get(key);
        
        if (entry != null) {
            if (entry.isExpired()) {
                cache.remove(key);
                return false;
            }
            return true;
        }
        
        return false;
    }
}
