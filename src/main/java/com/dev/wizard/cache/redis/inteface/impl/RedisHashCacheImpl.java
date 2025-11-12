package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.redis.JsonUtils;
import com.dev.wizard.cache.redis.inteface.RedisAbstractCache;
import com.dev.wizard.cache.redis.inteface.RedisRawHashCache;
import org.springframework.data.redis.core.HashOperations;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:53
 */
public class RedisHashCacheImpl<K, V> extends RedisAbstractCache implements RedisRawHashCache<K, V> {
    
    private HashOperations<String, String, String> operations;
    private Class<K> keyType;
    private Class<V> valueType;
    private Boolean preventCachePenetration;

    public RedisHashCacheImpl(HashOperations<String, String, String> operations, String prefix, Class<K> keyType, Class<V> valueType, Boolean preventCachePenetration) {
        this.operations = operations;
        this.prefix = prefix;
        this.keyType = keyType;
        this.valueType = valueType;
        this.preventCachePenetration = preventCachePenetration;
    }

    @Override
    public Long delete(String key, K... hashKeys) {
        return operations.delete(withPrefix(key), hashKeys);
    }

    @Override
    public Boolean hasKey(String key, K hashKey) {
        return operations.hasKey(withPrefix(key), hashKey);
    }

    @Override
    public V get(String key, K hashKey) {
        return JsonUtils.fromJson(operations.get(withPrefix(key), hashKey), valueType);
    }

    @Override
    public List<V> multiGet(String key, Collection<K> hashKeys) {
        Set<String> keySet = hashKeys.stream()
                .map(JsonUtils::toJson)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        List<String> result = operations.multiGet(withPrefix(key), keySet);
        return result.stream()
                .map(hashValue -> JsonUtils.fromJson(hashValue, valueType))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Set<K> keys(String key) {
        Set<String> keySet = operations.keys(withPrefix(key));
        return keySet.stream().map(hashKey -> JsonUtils.fromJson(hashKey, keyType)).collect(Collectors.toSet());
    }

    @Override
    public Long size(String key) {
        return operations.size(withPrefix(key));
    }

    @Override
    public void put(String key, K hashKey, V value) {
        operations.put(withPrefix(key), JsonUtils.toJson(hashKey), JsonUtils.toJson(value));
    }

    @Override
    public Boolean putIfAbsent(String key, K hashKey, V value) {
        return operations.putIfAbsent(withPrefix(key), JsonUtils.toJson(hashKey), JsonUtils.toJson(value));
    }

}
