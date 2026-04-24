package csusm.cougarplanner.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton registry that owns the app's in-memory caches and exposes them by
 * name ("courses", "assignments", "announcements"). Keeps callers from having
 * to pass the specific cache instance around, and keeps the set of caches in
 * one place so capacities can be tuned without hunting through the codebase.
 * <p>
 * Each named cache is an LRU cache backed by {@link LinkedListLRUCache};
 * concrete subclasses in {@link ConcreteCaches} pick per-type capacities.
 */
public class CacheManager {

    private final Map<String, Cache<?>> caches;

    // Cheap to construct and needed early in startup.
    private static final CacheManager INSTANCE = new CacheManager();

    public static CacheManager getInstance() {
        return INSTANCE;
    }

    private CacheManager() {
        caches = new HashMap<>();
        caches.put("courses",       new CourseCache());
        caches.put("assignments",   new AssignmentCache());
        caches.put("announcements", new AnnouncementCache());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, String key) {
        Cache<T> cache = (Cache<T>) caches.get(cacheName);
        if (cache == null) {
            return null;
        }
        return cache.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> void put(String cacheName, String key, T value) {
        Cache<T> cache = (Cache<T>) caches.get(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("No cache registered with name: " + cacheName);
        }
        cache.put(key, value);
    }
}
