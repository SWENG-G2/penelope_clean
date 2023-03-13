package sweng.penelope.controllers;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Utility class for caching operations
 */
public class CacheUtils {
    public static final String BIRDS = "birds";
    public static final String CAMPUSES = "campuses";
    public static final String CAMPUSES_LIST = "campusesList";
    public static final String ASSETS = "assets";

    private CacheUtils() {
    }

    /**
     * Evicts a cached element.
     * @param cacheManager A {@link CacheManager} instance
     * @param cacheName The name of the cache record to evict
     * @param key The caching key
     */
    public static void evictCache(CacheManager cacheManager, String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            if (key != null)
                cache.evictIfPresent(key);
            else
                cache.clear();
        }
    }
}
