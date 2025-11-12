package com.dev.wizard.cache.config;

import com.dev.wizard.cache.redis.MyAbstractRedisCache;
import com.dev.wizard.cache.redis.MyRedisCache;
import com.dev.wizard.cache.redis.anno.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.util.Arrays;

/**
 * Author: wizard.wu
 * Date: 2025/9/7 08:52
 */
@Component
public class RedisCacheBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Arrays.stream(bean.getClass().getDeclaredFields()).forEach(field -> {
            RedisCache redisCache = field.getAnnotation(RedisCache.class);
            if (null != redisCache) {
                ReflectionUtils.makeAccessible(field);
                MyRedisCache myCache = new MyAbstractRedisCache.Default(stringRedisTemplate, redisCache.value(), Boolean.TRUE.equals(redisCache.preventCachePenetration()));
                try {
                    field.set(bean, myCache);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject RedisCache for field: " + field.getName(), e);
                }
            }
        });
        return bean;
    }
}

