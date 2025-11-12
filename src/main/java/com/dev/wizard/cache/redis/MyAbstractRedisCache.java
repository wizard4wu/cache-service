package com.dev.wizard.cache.redis;

import com.dev.wizard.cache.redis.inteface.CollectionCache;
import com.dev.wizard.cache.redis.inteface.enhance.EnhanceRedisCache;
import com.dev.wizard.cache.redis.inteface.impl.RedisCacheImpl;
import com.dev.wizard.cache.redis.inteface.impl.RedisCollectionCacheImpl;
import lombok.experimental.Delegate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Author: wizard.wu
 * Date: 2025/9/7 09:36
 */
public abstract class MyAbstractRedisCache implements MyRedisCache {
    @Delegate
    private EnhanceRedisCache enhanceRedisCache;

    @Delegate
    private CollectionCache collectionCache;

    private final static String CACHE_PREFIX_VALUE = "my";

    private final static String SPLIT= "-";
    private final static String CACHE_VERSION = "v1";

    public MyAbstractRedisCache(StringRedisTemplate redisTemplate, String prefix, Boolean preventCachePenetration) {
        final String newPrefix = null == prefix ? CACHE_PREFIX_VALUE : prefix;
        enhanceRedisCache = new RedisCacheImpl(redisTemplate, newPrefix + SPLIT + CACHE_VERSION, preventCachePenetration);
        collectionCache = new RedisCollectionCacheImpl(redisTemplate, newPrefix + SPLIT + CACHE_VERSION, preventCachePenetration);
    }
    public static class Default extends MyAbstractRedisCache {
        public Default(StringRedisTemplate redisTemplate, String prefix, Boolean preventCachePenetration) {
            super(redisTemplate, prefix, preventCachePenetration);
        }
    }
}
