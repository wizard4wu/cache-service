package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.redis.JsonUtils;
import com.dev.wizard.cache.redis.inteface.RedisAbstractCache;
import com.dev.wizard.cache.redis.inteface.RedisRawSetCache;
import org.springframework.data.redis.core.SetOperations;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:54
 */
public class RedisSetCacheImpl<V> extends RedisAbstractCache implements RedisRawSetCache<V> {

    private SetOperations<String, String> operations;
    private Boolean preventCachePenetration;
    private Class<V> clazz;

    public RedisSetCacheImpl(SetOperations<String, String> operations, String prefix, Class<V> clazz, Boolean preventCachePenetration) {
        this.operations = operations;
        this.prefix = prefix;
        this.clazz = clazz;
        this.preventCachePenetration = preventCachePenetration;
    }

    @Override
    public Long add(String key, V value) {
        return operations.add(withPrefix(key), JsonUtils.toJson(value));
    }

    @Override
    public Set<V> members(String key) {
        Set<String> jsonValues = operations.members(withPrefix(key));
        return jsonValues.stream()
                .map(value -> JsonUtils.fromJson(value, clazz))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Boolean isMember(String key, V value) {
        return operations.isMember(withPrefix(key), JsonUtils.toJson(value));
    }

    @Override
    public Long remove(String key, V value) {
        return operations.remove(withPrefix(key), JsonUtils.toJson(value));
    }

    @Override
    public Long size(String key) {
        return operations.size(withPrefix(key));
    }
}
