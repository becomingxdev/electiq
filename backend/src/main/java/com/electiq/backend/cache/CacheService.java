package com.electiq.backend.cache;

/**
 * Interface for caching operations.
 * Allows switching between in-memory and distributed cache implementations.
 */
public interface CacheService {
    /**
     * Retrieves a value from the cache.
     * @param key The unique key for the cached item.
     * @return The cached value, or null if not found.
     */
    String get(String key);

    /**
     * Stores a value in the cache.
     * @param key The unique key for the item.
     * @param value The value to store.
     */
    void set(String key, String value);

    /**
     * Checks if a key exists in the cache.
     * @param key The key to check.
     * @return true if the key exists, false otherwise.
     */
    boolean contains(String key);
}
