package com.dev.wizard.cache.redis.anno;

import java.lang.annotation.*;

/**
 * Author: wizard.wu
 * Date: 2025/9/6 09:09
 */
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCache {
    String value() default "my";

    boolean preventCachePenetration() default false; //防止缓存穿透
}
