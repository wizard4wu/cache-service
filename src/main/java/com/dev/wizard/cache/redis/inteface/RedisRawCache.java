package com.dev.wizard.cache.redis.inteface;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: wizard.wu
 * Date: 2025/9/6 09:11
 */
public interface RedisRawCache {

    int DEFAULT_EXP = 30;

    long CACHE_PROTECT_DEFAULT_EXP = 1L;
    TimeUnit DEFAULT_EXP_UNIT = TimeUnit.MINUTES;

    <T> void set(String key, T value);

    <T> void set(String key, T value, long timeout, TimeUnit unit);

    <T> Boolean setIfAbsent(String key, T value);
    <T> Boolean setIfAbsent(String key, T value, long timeout, TimeUnit unit);

    <T> void multiSet(Map<String, T> map);

    <T> Boolean multiSetIfAbsent(Map<String, T> map);

    <T> T get(String key, Class<T> clazz);
    <T> T getAndDelete(String key, Class<T> clazz);

    Long increment(String key, long delta);

    Long decrement(String key, long delta);

    Boolean hasKey(String key);

    Boolean delete(String key);

    Long delete(Collection<String> keys);
}
