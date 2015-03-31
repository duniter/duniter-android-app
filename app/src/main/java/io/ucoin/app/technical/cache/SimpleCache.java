package io.ucoin.app.technical.cache;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by eis on 30/03/15.
 */
public abstract class SimpleCache<K, V> {

    private static final long ETERNAL_TIME = -1l;

    private Map<K, V> mCachedValues;
    private Map<K, Long> mCachedTimes;
    private long mCacheTimeInMillis;

    public SimpleCache() {
        this(ETERNAL_TIME);
    }

    public SimpleCache(long cacheTimeInMillis) {
        mCachedValues = new HashMap<K, V>();
        mCachedTimes = new HashMap<K, Long>();
        mCacheTimeInMillis = cacheTimeInMillis;
    }

    public V getIfPresent(K key) {
        V cachedValue = mCachedValues.get(key);
        long timeInMillis = System.currentTimeMillis();
        if (cachedValue != null) {
            Long cachedTime = mCachedTimes.get(key);
            if (mCacheTimeInMillis == ETERNAL_TIME
                    || cachedTime.longValue() - timeInMillis < mCacheTimeInMillis) {
                return cachedValue;
            }
        }

        return null;
    }

    /**
     * Get the cached value. If not already loaded, <code>load()</code>
     * will be called.
     * @param key
     * @return
     */
    public V get(Context context, K key) {
        V cachedValue = mCachedValues.get(key);
        long timeInMillis = System.currentTimeMillis();
        if (cachedValue != null) {
            Long cachedTime = mCachedTimes.get(key);
            if (cachedTime.longValue() - timeInMillis < mCacheTimeInMillis) {
                return cachedValue;
            }
        }

        // Load a new value
        cachedValue = load(context, key);

        // Fill caches
        mCachedValues.put(key, cachedValue);
        mCachedTimes.put(key, timeInMillis);

        return cachedValue;
    }

    /**
     * Set a value into the cache
     * @param key
     * @param value
     */
    public void put(K key, V value) {
        // Fill caches
        mCachedValues.put(key, value);
        mCachedTimes.put(key, System.currentTimeMillis());
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
        return mCachedValues.keySet();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set<Map.Entry<K,V>> entrySet() {
        return mCachedValues.entrySet();
    }

    /**
     * Clear cached values
     */
    public void clear() {
        mCachedValues.clear();
        mCachedTimes.clear();
    }

    public abstract V load(Context context, K key);

}
