package csusm.cougarplanner.cache;

/**
 * A generic cache interface defining the core operations all caches must support.
 *
 * @param <T> the type of value stored in the cache
 */
public interface Cache<T> {

    /**
     * Retrieves the cached value associated with the given key.
     *
     * @param key the lookup key
     * @return the cached value, or {@code null} if the key is not present
     */
    T get(String key);

    /**
     * Inserts or updates the value associated with the given key.
     *
     * @param key   the cache key
     * @param value the value to store
     */
    void put(String key, T value);

    /**
     * Removes the entry associated with the given key, if it exists.
     *
     * @param key the key of the entry to remove
     */
    void remove(String key);

    /**
     * Returns the current number of entries in the cache.
     *
     * @return the number of cached entries
     */
    int size();
}