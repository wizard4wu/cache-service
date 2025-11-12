package com.dev.wizard.cache.redis.inteface.enhance;

import com.dev.wizard.cache.redis.inteface.RedisRawCache;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public interface EnhanceRedisCache extends RedisRawCache {

    <T> T getAndSet(String key, T value, Class<T> clazz, long exp, TimeUnit unit);

    <V> V get(String key, Class<V> type, Supplier<V> supplier);

    <V> V get(String key, Class<V> type, Supplier<V> supplier, long exp, TimeUnit unit);

    <V> V get(String key, TypeReference<V> type);

    <V> V get(String key, TypeReference<V> type, Supplier<V> supplier);

    <V> V get(String key, TypeReference<V> type, Supplier<V> supplier, long exp, TimeUnit unit);

    <V> Map<String, V> multiGet(Collection<String> keys, TypeReference<V> type);

    <V> Map<String, V> multiGet(List<String> keys, Class<V> type, Function<List<String>, Map<String, V>> function, long exp, TimeUnit unit);
}
