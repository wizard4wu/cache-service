package com.dev.wizard.cache;

import com.dev.wizard.cache.config.RedisAutoConfig;
import com.dev.wizard.cache.redis.MyRedisCache;
import com.dev.wizard.cache.service.OrderService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * Author: wizard.wu
 * Date: 2025/9/7 10:48
 */

public class CacheApplicationStarter {
    public static void main(String[] args) {
       AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RedisAutoConfig.class);
       MyRedisCache cache = context.getBean(MyRedisCache.class);
       cache.set("shseeeessss", "重中之重", 10, TimeUnit.DAYS);


        OrderService lockAspect = context.getBean(OrderService.class);
        lockAspect.createOrder(1111L);
        context.close();
    }
}
