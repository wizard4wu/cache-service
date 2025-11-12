package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.redis.AbstractCache;
import com.dev.wizard.cache.redis.JsonUtils;
import com.dev.wizard.cache.redis.data.domain.Payload;
import com.dev.wizard.cache.redis.inteface.RedisAbstractCache;
import com.dev.wizard.cache.redis.inteface.RedisRawListCache;
import org.springframework.data.redis.core.ListOperations;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:53
 */
public class RedisListCacheImpl<V> extends RedisAbstractCache implements RedisRawListCache<V> {

    private ListOperations<String, String> operations;
    private Boolean preventCachePenetration;
    private Class<V> clazz;


    public RedisListCacheImpl(ListOperations<String, String> operations, String prefix, Class<V> clazz, Boolean preventCachePenetration) {
        this.operations = operations;
        this.prefix = prefix;
        this.clazz = clazz;
        this.preventCachePenetration = preventCachePenetration;
    }


    @Override
    public Long leftPush(String key, V value) {
        return operations.leftPush(withPrefix(key), JsonUtils.toJson(value));
    }

    @Override
    public Long rightPush(String key, V value) {
        return operations.rightPush(withPrefix(key), JsonUtils.toJson(value));
    }

    @Override
    public Long rightPushAll(String key, Collection<V> values) {
        List<String> jsonValues = values.stream().map(JsonUtils::toJson).collect(Collectors.toList());
        return operations.rightPushAll(withPrefix(key), jsonValues);
    }

    @Override
    public Long leftPushIfPresent(String key, V value) {
        return operations.leftPushIfPresent(withPrefix(key), JsonUtils.toJson(value));
    }

    @Override
    public Long rightPushIfPresent(String key, V value) {
        return operations.rightPushIfPresent(withPrefix(key), JsonUtils.toJson(value));
    }

    @Override
    public V index(String key, long index) {
        String jsonValue = operations.index(withPrefix(key), index);
        return JsonUtils.fromJson(jsonValue, clazz);
    }

    @Override
    public List<V> range(String key, long start, long end) {
        List<String> jsonValues = operations.range(withPrefix(key), start, end);
        return jsonValues.stream()
                .map(jsonValue -> JsonUtils.fromJson(jsonValue, clazz))
                .collect(Collectors.toList());
    }

    @Override
    public Long size(String key) {
        return operations.size(withPrefix(key));
    }

    @Override
    public V rightPop(String key) {
        String jsonValue = operations.rightPop(withPrefix(key));
        return JsonUtils.fromJson(jsonValue, clazz);
    }

    @Override
    public V leftPop(String key) {
        String jsonValue = operations.leftPop(withPrefix(key));
        return JsonUtils.fromJson(jsonValue, clazz);
    }

}
