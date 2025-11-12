package com.dev.wizard.cache.redis.inteface;

/**
 * Author: wizard.wu
 * Date: 2025/11/9 20:48
 */
public abstract class RedisAbstractCache {

    protected String prefix;

    protected String withPrefix(String key) {
        return prefix + ":" + key;
    }
}
