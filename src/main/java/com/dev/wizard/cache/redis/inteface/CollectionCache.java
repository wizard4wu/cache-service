package com.dev.wizard.cache.redis.inteface;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:40
 */
public interface CollectionCache {

    <V> RedisRawListCache<V> listCache(Class<V> type);

    <V> RedisRawSetCache<V> setCache(Class<V> type);

    <K, V> RedisRawHashCache<K, V> hashCache(Class<K> hashKeyType, Class<V> valueType);

    <V> RedisRawSortedSetCache<V> sortedSetCache(Class<V> type);

}
