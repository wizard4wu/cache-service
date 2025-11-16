package com.dev.wizard.cache.redis.inteface.impl;


import com.dev.wizard.cache.redis.JsonUtils;
import com.dev.wizard.cache.redis.inteface.RedisAbstractCache;
import com.dev.wizard.cache.redis.inteface.RedisRawSortedSetCache;
import org.springframework.data.redis.core.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: wizard.wu
 * Date: 2025/9/11 23:32
 */
public class RedisRedisSortedSetCacheImpl<V> extends RedisAbstractCache implements RedisRawSortedSetCache<V> {

    private ZSetOperations<String, String> operations;
    private Boolean preventCachePenetration;
    private Class<V> clazz;

    public RedisRedisSortedSetCacheImpl(ZSetOperations<String, String> operations, String prefix, Class<V> clazz, Boolean preventCachePenetration) {
        this.operations = operations;
        this.prefix = prefix;
        this.clazz = clazz;
        this.preventCachePenetration = preventCachePenetration;
    }
    @Override
    public Boolean add(String key, V value, double score) {
        return operations.add(withPrefix(key), JsonUtils.toJson(value), score);
    }

    @Override
    public Boolean addIfAbsent(String key, V value, double score) {
        return operations.addIfAbsent(withPrefix(key), JsonUtils.toJson(value), score);
    }

    @Override
    public Long remove(String key, V value) {
        return operations.remove(withPrefix(key), JsonUtils.toJson(value));
    }

    @Override
    public Set<V> rangeByScore(String key, double min, double max) {
        Set<String> jsonValues = operations.rangeByScore(withPrefix(key), min, max);
        return jsonValues.stream()
                .map(json -> JsonUtils.fromJson(json, clazz))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Long size(String key) {
        return operations.size(withPrefix(key));
    }
}
