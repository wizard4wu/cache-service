package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.redis.JsonUtils;
import com.dev.wizard.cache.redis.data.domain.Payload;
import com.dev.wizard.cache.redis.inteface.RedisAbstractCache;
import com.dev.wizard.cache.redis.inteface.enhance.EnhanceRedisCache;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Author: wizard.wu
 * Date: 2025/9/11 23:27
 */
public class RedisCacheImpl extends RedisAbstractCache implements EnhanceRedisCache {
    private StringRedisTemplate redisTemplate;

    private Boolean preventCachePenetration;


    public RedisCacheImpl(StringRedisTemplate redisTemplate, String prefix, Boolean preventCachePenetration) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
        this.preventCachePenetration = preventCachePenetration;
    }

    @Override
    public <T> void set(String key, T value) {
        set(key, value, DEFAULT_EXP, DEFAULT_EXP_UNIT);
    }

    @Override
    public <T> void set(String key, T value, long exp, TimeUnit unit) {
        Payload<T> payload = Payload.newPayload(value, preventCachePenetration);
        final long expiredTime = payload.fromEmpty() ? CACHE_PROTECT_DEFAULT_EXP : exp;
        redisTemplate.opsForValue()
                .set(withPrefix(key), payload.serialize(), expiredTime, unit);
    }

    @Override
    public <T> Boolean setIfAbsent(String key, T value) {
        return setIfAbsent(key, value, DEFAULT_EXP, DEFAULT_EXP_UNIT);
    }

    @Override
    public <T> Boolean setIfAbsent(String key, T value, long exp, TimeUnit unit) {
        Payload<T> payload = Payload.newPayload(value, preventCachePenetration);
        final long expiredTime = payload.fromEmpty() ? CACHE_PROTECT_DEFAULT_EXP : exp;
        return redisTemplate
                .opsForValue()
                .setIfAbsent(withPrefix(key), payload.serialize(), expiredTime, unit);
    }

    @Override
    public <T> void multiSet(Map<String, T> map) {
        if(map == null || map.isEmpty()) {
            return;
        }
        Map<String, String> mapWithPrefix = new HashMap<>();
        for(Map.Entry<String, T> entry : map.entrySet()) {
            mapWithPrefix.put(withPrefix(entry.getKey()), Payload.newPayload(entry.getValue(), preventCachePenetration).serialize());
        }
        redisTemplate.opsForValue().multiSet(mapWithPrefix);
    }

    @Override
    public <T> Boolean multiSetIfAbsent(Map<String, T> map) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        Map<String, String> jsonMap = new HashMap<>();
        map.forEach((key, value) -> jsonMap.put(withPrefix(key), Payload.newPayload(value, preventCachePenetration).serialize()));
        return redisTemplate.opsForValue().multiSetIfAbsent(jsonMap);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        final String jsonValue = redisTemplate.opsForValue().get(withPrefix(key));
        return Payload.deserialize(jsonValue, clazz).getValue();
    }

    @Override
    public <T> T getAndDelete(String key, Class<T> clazz) {
        final String jsonValue = redisTemplate.opsForValue().getAndDelete(withPrefix(key));
        return Payload.deserialize(jsonValue, clazz).getValue();
    }

    @Override
    public <T> T getAndSet(String key, T value, Class<T> clazz, long exp, TimeUnit unit) {
        final String keyWithPrefix = withPrefix(key);
        Payload<T> payload = Payload.newPayload(value, preventCachePenetration);
        String oldJsonValue = redisTemplate.opsForValue().getAndSet(keyWithPrefix, payload.serialize());
        if(exp > 0 && unit != null) {
            redisTemplate.expire(keyWithPrefix, exp, unit);
        }
        return  Payload.deserialize(oldJsonValue, clazz).getValue();
    }

    @Override
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(withPrefix(key), delta);
    }

    @Override
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(withPrefix(key), delta);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(withPrefix(key));
    }


    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(withPrefix(key));
    }

    @Override
    public Long delete(Collection<String> keys) {
        List<String> keysWithPrefix = keys.stream().map(this::withPrefix).collect(Collectors.toList());
        return redisTemplate.delete(keysWithPrefix);
    }
    @Override
    public <V> V get(String key, Class<V> type, Supplier<V> supplier) {
        return get(key, type, supplier, DEFAULT_EXP, DEFAULT_EXP_UNIT);
    }

    @Override
    public <V> V get(String key, Class<V> clazz, Supplier<V> supplier, long exp, TimeUnit unit) {
        String redisKey = withPrefix(key);
        String jsonValue = redisTemplate.opsForValue().get(redisKey);
        if(null != jsonValue) {
            Payload<V> payload = Payload.deserialize(jsonValue, clazz);
            return payload.getValue();
        }
        if (null == supplier){
            return null;
        }
        V value = supplier.get();
        set(key, value, exp, unit);
        return value;
    }

    @Override
    public <V> V get(String key, TypeReference<V> type) {
        String json = redisTemplate.opsForValue().get(withPrefix(key));
        if (json == null) {
            return null;
        }
        return JsonUtils.fromJson(json, type);
    }

    @Override
    public <V> V get(String key, TypeReference<V> type, Supplier<V> supplier) {
        return get(key, type, supplier, DEFAULT_EXP, DEFAULT_EXP_UNIT);
    }

    @Override
    public <V> V get(String key, TypeReference<V> type, Supplier<V> supplier, long exp, TimeUnit unit) {
        String jsonValue = redisTemplate.opsForValue().get(withPrefix(key));
        if(null != jsonValue) {
            Payload<V> payload = Payload.deserialize(jsonValue, type);
            return payload.getValue();
        }
        if(null == supplier){
            return null;
        }
        V value = supplier.get();
        set(key, value, exp, unit);
        return value;
    }

    @Override
    public <V> Map<String, V> multiGet(Collection<String> keys, TypeReference<V> type) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> keyMap = keys.stream()
                .collect(Collectors.toMap(key -> withPrefix(key), key -> key, (prev, next) -> next));
        List<String> keyWithPrefixList = keys.stream()
                .map(this::withPrefix)
                .collect(Collectors.toList());

        List<String> resultValue = redisTemplate.opsForValue().multiGet(keyWithPrefixList);

        Map<String, V> resultMap = new HashMap<>(keys.size());
        for (int index = 0; index < keys.size(); index++) {
            String json = resultValue.get(index);
            Payload<V> payload = Payload.deserialize(json, type);
            resultMap.put(keyMap.get(keyWithPrefixList.get(index)), payload.getValue()); // 使用原始 key 作为 map key
        }
        return resultMap;
    }

    @Override
    public <V> Map<String, V> multiGet(List<String> keys, Class<V> type, Function<List<String>, Map<String, V>> function, long exp, TimeUnit unit) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> cacheKeysWithPrefix = keys.stream().map(this::withPrefix).collect(Collectors.toList());
        //从redis中获取结果
        List<String> resultFromRedis = redisTemplate.opsForValue().multiGet(cacheKeysWithPrefix);

        List<String> missedKeys = new ArrayList<>();

        Map<String, V> resultMap = new HashMap<>();

        for(int index = 0; index < cacheKeysWithPrefix.size(); index++) {
            final String rawValue = resultFromRedis.get(index);
            final String key = keys.get(index);
            if(null == rawValue) {
                missedKeys.add(key);
            }else {
                resultMap.put(key, Payload.deserialize(rawValue, type).getValue());
            }
        }
        //对没有命中redis的key执行对应的函数，然后将返回的数据set到redis中
        if(null != function && !missedKeys.isEmpty()) {
            Map<String, V> loaded = function.apply(missedKeys);
            missedKeys.forEach(key -> {
                V value = null != loaded ? loaded.get(key) : null;
                set(key, value, exp, unit);
            });
            if(null != loaded && !loaded.isEmpty()) {
                resultMap.putAll(loaded);
            }
        }
        return resultMap;
    }
}
