package com.dev.wizard.cache.redis.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisIdempotent {

    String key();
    long expireTime() default 24 * 60 * 60 * 1000; // unit is ms, -1 means no expiration
}
