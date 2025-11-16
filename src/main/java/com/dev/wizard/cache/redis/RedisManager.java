package com.dev.wizard.cache.redis;


import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class RedisManager {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    public <T> T executeWithIdempotent(String key, long expireTimeMillSeconds, Supplier<T> supplierForBiz) {
        if (supplierForBiz == null) {
            throw new IllegalArgumentException("business logic supplier can not be null");
        }

        if (key == null || key.trim().isEmpty()) {
            log.warn("key is null or empty, executing business logic without idempotency guarantee");
            return supplierForBiz.get();
        }
        final Boolean seted = expireTimeMillSeconds > 0
                ? stringRedisTemplate.opsForValue().setIfAbsent(key, "1", expireTimeMillSeconds, TimeUnit.MILLISECONDS)
                : stringRedisTemplate.opsForValue().setIfAbsent(key, "1");
        if (Boolean.TRUE.equals(seted)) {
            try {
                return supplierForBiz.get();
            } catch (Exception e) {
                // In case of exception during business logic execution, delete the key to allow retries
                stringRedisTemplate.delete(key);
                throw new RuntimeException("exception during business logic execution, key: " + key, e);
            }
        } else {
            throw new RuntimeException("duplicate request detected, key: " + key);
        }
    }

    /**
     * 使用 Redisson 执行带分布式锁的业务逻辑
     *
     * @param lockKey         锁的 key（建议格式：lock:business:xxx）
     * @param lockExpiredTime 锁的自动过期时间（防死锁), <= 0 表示不设置自动过期时间，单位：毫秒
     * @param tryLockTime     最多等待获取锁的时间，单位：毫秒
     * @param suppierForBiz   业务逻辑（Supplier 函数式接口）
     * @param <T>             返回值类型
     * @return 业务逻辑执行结果
     * @throws RuntimeException 获取锁超时或执行异常
     */
    public <T> T executeWithRedisLock(String lockKey, long lockExpiredTime, long tryLockTime, Supplier<T> suppierForBiz) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            throw new IllegalArgumentException("lockKey can not be null or empty");
        }
        if (suppierForBiz == null) {
            throw new IllegalArgumentException("business logic supplier can not be null");
        }
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // try to acquire the lock, await up to tryLockTime seconds
            boolean isLocked = lock.tryLock(tryLockTime, lockExpiredTime, TimeUnit.MILLISECONDS);
            // Failed to acquire lock within tryLockTime
            if (!isLocked) {
                throw new RuntimeException("failed to try lock, lockKey: " + lockKey);
            }
            // 成功获取锁，执行业务逻辑
            return suppierForBiz.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted while trying to acquire lock, lockKey: " + lockKey, e);
        } catch (Exception e) {
            throw new RuntimeException("exception during business logic execution, lockKey: " + lockKey, e);
        } finally {
            // Note：only current thread can unlock
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
