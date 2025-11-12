package com.dev.wizard.cache.redis.inteface;

import java.util.Set;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:42
 */
public interface RedisRawSortedSetCache<V> {

    Boolean add(String key, V value, double score);


    Boolean addIfAbsent(String key, V value, double score);

    Long remove(String key, V value);

    Set<V> rangeByScore(String key, double min, double max);

    Long size(String key);
}
