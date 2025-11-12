package com.dev.wizard.cache.service;

import com.dev.wizard.cache.redis.MyRedisCache;
import com.dev.wizard.cache.redis.anno.RedisCache;
import com.dev.wizard.cache.redis.anno.RedisLock;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OrderService {

    @RedisCache("order")
    private MyRedisCache myCache;
    @RedisLock(key = "'order:' + #orderId")
    public void createOrder(Long orderId) {
        myCache.set("eee", "orderData", 5, TimeUnit.HOURS);
        System.out.println("执行下单逻辑");
    }
}
