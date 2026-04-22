package csusm.cougarplanner.cache;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private final Map<String, Cache<?>> caches;

    private static final CacheManager INSTANCE = new CacheManager();

    public static CacheManager getInstance() {
        return INSTANCE;
    }

    private CacheManager() {
        caches = new HashMap<>();
        caches.put("courses",       new CourseCache());
        caches.put("assignments",   new AssignmentCache());
        caches.put("announcements", new AnnouncementCache());
        caches.put("tasks",         new TaskCache());
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