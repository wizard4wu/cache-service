package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.redis.inteface.*;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:44
 */
public class RedisCollectionCacheImpl implements CollectionCache {

    private StringRedisTemplate redisTemplate;
    private String prefix;
    private Boolean preventCachePenetration;

    public RedisCollectionCacheImpl(StringRedisTemplate redisTemplate, String prefix, Boolean preventCachePenetration) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
        this.preventCachePenetration = preventCachePenetration;
    }


    @Override
    public <V> RedisRawListCache<V> listCache(Class<V> clazz) {
        return new RedisListCacheImpl<>(redisTemplate.opsForList(), prefix, clazz, preventCachePenetration);
    }

    @Override
    public <V> RedisRawSetCache<V> setCache(Class<V> clazz) {
        return new RedisSetCacheImpl<>(redisTemplate.opsForSet(), prefix, clazz, preventCachePenetration) ;
    }

    @Override
    public <K, V> RedisRawHashCache<K, V> hashCache(Class<K> hashKeyType, Class<V> valueType) {
        return new RedisHashCacheImpl<>(redisTemplate.opsForHash(), prefix, hashKeyType, valueType, preventCachePenetration);
    }

    @Override
    public <V> RedisRawSortedSetCache<V> sortedSetCache(Class<V> clazz) {
        return new RedisRedisRawSortedSetCacheImpl<>(redisTemplate.opsForZSet(), prefix, clazz, preventCachePenetration);
    }
}
