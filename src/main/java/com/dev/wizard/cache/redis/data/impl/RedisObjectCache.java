package com.dev.wizard.cache.redis.data.impl;

import com.dev.wizard.cache.redis.AbstractCache;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Author: wizard.wu
 * Date: 2025/9/6 09:13
 */

public class RedisObjectCache extends AbstractCache {

    private String prefix;
    private StringRedisTemplate redisTemplate;


    public RedisObjectCache(StringRedisTemplate redisTemplate, String prefix) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
    }




}
