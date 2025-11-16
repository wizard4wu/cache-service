package com.dev.wizard.cache.redis.anno;

import java.lang.annotation.*;

/**
 * Author: wizard.wu
 * Date: 2025/9/6 09:10
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RedisLock {

    String key();

    long lockExpiredTime() default -1; //-1 help to enable watchdog，unit is ms

    long tryLockTime() default 2_000;  //unit is ms
}
