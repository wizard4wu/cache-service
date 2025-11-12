package com.dev.wizard.cache.redis.inteface;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:41
 */
public interface RedisRawHashCache<K, V>{


    Long delete(String key, K... hashKeys);

    Boolean hasKey(String key, K hashKey);

    V get(String key, K hashKey);

    List<V> multiGet(String key, Collection<K> hashKeys);

    Set<K> keys(String key);

    Long size(String key);

    void put(String key, K hashKey, V value);

    Boolean putIfAbsent(String key, K hashKey, V value);
}
