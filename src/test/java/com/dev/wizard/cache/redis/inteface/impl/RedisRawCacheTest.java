package com.dev.wizard.cache.redis.inteface.impl;


import com.dev.wizard.cache.config.RedisAutoConfig;
import com.dev.wizard.cache.domain.TestUser;
import com.dev.wizard.cache.redis.MyRedisCache;
import com.dev.wizard.cache.redis.anno.RedisCache;
import com.dev.wizard.cache.redis.anno.RedisLock;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = RedisAutoConfig.class)
public class RedisRawCacheTest {

    @RedisCache(value = "test1")
    private MyRedisCache redisCache;

    @RedisCache(value = "test2", preventCachePenetration = true)
    private MyRedisCache preventCachePenetrationRedisCache;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserService userService;

    //@BeforeEach
    void setUp() {
        // 清空测试数据，保证每次测试干净
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void testSetAndGet() {
        final String userKey = "user:1001";
        TestUser user = new TestUser(9911112222L, "name", Instant.now());
        // set 方法
        redisCache.set(userKey, user);
        // get 方法
        TestUser expectedUser = redisCache.get(userKey, TestUser.class);

        Assertions.assertEquals(user, expectedUser);

        preventCachePenetrationRedisCache.set(userKey, null);
        TestUser nullUser = preventCachePenetrationRedisCache.get(userKey, TestUser.class);
        assertNull(nullUser);
    }

    @Test
    public void testRedisLock() throws InterruptedException {

        TestUser user = new TestUser(12345L, "name", Instant.now());

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    userService.createUser(user);  // 同一个 userId
                } catch (Exception ignored) {}
                finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        // 断言只有 1 个线程成功进入临界区
        System.out.println("最终调用次数 = " + userService.getCallCount());
        Assertions.assertEquals(1, userService.getCallCount(),
                "分布式锁失败，应当只有 1 个线程成功执行 createUser()");

    }





    @Test
    void testSetIfAbsent() {
        TestUser user1 = new TestUser(9911112222L, "name", Instant.now());
        final String userKey = "user:1002";


        boolean result1 = redisCache.setIfAbsent(userKey, user1);
        Assertions.assertTrue(result1);
        // 再尝试 setIfAbsent
        TestUser user2 = new TestUser(2002L, "David", Instant.now());
        boolean result2 = redisCache.setIfAbsent(userKey, user2);
        Assertions.assertFalse(result2);

        preventCachePenetrationRedisCache.setIfAbsent(userKey, null);
        boolean result3 = preventCachePenetrationRedisCache.setIfAbsent(userKey, user2);
        Assertions.assertFalse(result3);
        // 再尝试 setIfAbsent
        boolean result4 = preventCachePenetrationRedisCache.setIfAbsent(userKey, user1);
        Assertions.assertFalse(result4);

    }

    @Test
    public void test_GetAndSet(){
        String key = "user:1";
        TestUser user1 = new TestUser(9911112222L, "name", Instant.now());
        redisCache.set(key, null);

        TestUser testUser = redisCache.get(key, TestUser.class);
        Assertions.assertNull(testUser);

        TestUser user2 = new TestUser(9911112222L, "name", Instant.now());
        List<TestUser> users = Arrays.asList(user1, user2);

        redisCache.set(key, users, 60, TimeUnit.SECONDS);

        String key2 = "user:2";
        redisCache.set(key2, null);

        TestUser result2 = redisCache.get(key2, TestUser.class);

        List<TestUser> userResult = redisCache.get(key, new TypeReference<List<TestUser>>() {});

        System.out.println(userResult);
    }

    @Test
    void testGetAndDelete() {
        TestUser user = new TestUser(1L, "Alice", Instant.now());

        String key = "user:GetAndDelete";
        // 写入缓存
        redisCache.set(key, user);

        // 调用 getAndDelete
        TestUser cached = redisCache.getAndDelete(key, TestUser.class);

        assertNotNull(cached);
        Assertions.assertEquals(user, cached);
        // 再次获取应该为 null
        TestUser afterDelete = redisCache.get(key, TestUser.class);
        assertNull(afterDelete);
    }

    @Test
    void testGetAndSet_returnsOldValue() throws InterruptedException {
        String key = "user:GetAndSet";
        // 初始缓存值
        TestUser oldUser = new TestUser(1L, "Alice", Instant.parse("1990-01-01T00:00:00Z"));
        redisCache.set(key, oldUser);
        // 新值
        TestUser newUser = new TestUser(2L, "Bob", Instant.parse("1991-02-02T00:00:00Z"));
        // 调用 getAndSet，设置过期 2 秒
        TestUser returned = redisCache.getAndSet(key, newUser, TestUser.class, 200, TimeUnit.SECONDS);
        // 返回的应该是旧值
        Assertions.assertEquals(oldUser, returned);
        // 当前缓存应该是新值
        TestUser current = redisCache.get(key, TestUser.class);
        Assertions.assertEquals(newUser, current);
    }

    @Test
    void testGet_whenCacheMiss_shouldUseSupplierAndCacheResult() {
        String key = "user:getWithSupplier";

        TestUser result1 = redisCache.get(key, TestUser.class, () -> null);

        assertNull(result1);
        TestUser suppliedUser = new TestUser(2L, "Bob", Instant.parse("1991-02-02T00:00:00Z"));
        TestUser result2 = redisCache.get(key, TestUser.class, () -> suppliedUser);
        Assertions.assertEquals(suppliedUser, result2);
        // 验证 Redis 中确实有值
        TestUser testUser = redisCache.get(key, TestUser.class);
        assertNotNull(testUser);

        //缓存保护
        String key2 = "user:getProtectedWithSupplier";

        TestUser result21 = preventCachePenetrationRedisCache.get(key2, TestUser.class, () -> null);
        assertNull(result21);
        TestUser suppliedUser2 = new TestUser(22L, "Bob2", Instant.parse("1991-02-02T00:00:00Z"));
        TestUser result22 = preventCachePenetrationRedisCache.get(key2, TestUser.class, () -> suppliedUser2);
        Assertions.assertEquals(null, result22);
        // 验证 Redis 中确实有值
        TestUser testUser2 = preventCachePenetrationRedisCache.get(key2, TestUser.class);
        assertNull(testUser2);
    }

    @Test
    void testGet_typeReference() {
        String key = "user:getWithSupplierTypeReference";

        TestUser result1 = redisCache.get(key, TestUser.class, () -> null);
        assertNull(result1);
        TestUser suppliedUser = new TestUser(2L, "Bob", Instant.parse("1991-02-02T00:00:00Z"));
        TestUser suppliedUser2 = new TestUser(2L, "Bob", Instant.parse("1991-02-02T00:00:00Z"));
        List<TestUser> expected1 = Arrays.asList(suppliedUser, suppliedUser2);
        List<TestUser> result2 = redisCache.get(key, new TypeReference<>() {}, () -> expected1);
        Assertions.assertEquals(expected1, result2);
        //验证 Redis 中确实有值
        List<TestUser> testUser = redisCache.get(key, new TypeReference<>() {});
        Assertions.assertEquals(expected1, testUser);


        //缓存保护
        String key2 = "user:getWithProtectedSupplierTypeReference";
        List<TestUser> result3 = preventCachePenetrationRedisCache.get(key2, new TypeReference<>() {}, () -> null);
        assertNull(result3);
        TestUser suppliedUser3 = new TestUser(2L, "Bob", Instant.parse("1991-02-02T00:00:00Z"));
        TestUser suppliedUser4 = new TestUser(2L, "Bob", Instant.parse("1991-02-02T00:00:00Z"));
        List<TestUser> expected2 = Arrays.asList(suppliedUser3, suppliedUser4);
        List<TestUser> result4 = preventCachePenetrationRedisCache.get(key2, new TypeReference<>() {}, () -> expected2);
        Assertions.assertNull(result4);
    }

    @Test
    void testIncrementLongDelta_realRedis() {
        String key = "counter:test";
        redisTemplate.delete(key);
        Long first = redisCache.increment(key, 5);
        Long second = redisCache.increment(key, 4);
        assertEquals(5L, first);
        assertEquals(9L, second);
        Long third = redisCache.decrement(key, 2);
        assertEquals(7L, third);
    }


    @Test
    void testDeleteSingleKey() {
        String key = "testKey1";
        redisCache.set(key, "value1");

        Boolean deleted = redisCache.delete(key);
        assertThat(deleted).isTrue();

        String value = redisCache.get(key, String.class);
        assertThat(value).isNull();

        String key2 = "testKey2";
        redisCache.set(key2, "value2");
        String key3 = "testKey3";
        redisCache.set(key3, "value3");

        boolean result = redisCache.hasKey(key2);
        assertThat(result).isTrue();


        List<String> keys = Arrays.asList(key2, key3);
        redisCache.delete(keys);
        String value2 = redisCache.get(key2, String.class);
        String value3 = redisCache.get(key3, String.class);
        assertThat(value2).isNull();
        assertThat(value3).isNull();
    }

    @Test
    void testDeleteMultipleKeys() {
        List<String> keys = Arrays.asList("k1", "k2", "k3");
        keys.forEach(k -> redisCache.set(k, "val_" + k));

        Long deletedCount = redisCache.delete(keys);
        assertThat(deletedCount).isEqualTo(3L);

        keys.forEach(k -> assertThat(redisTemplate.opsForValue().get("prefix:" + k)).isNull());
    }

    @Test
    void testMultiSetIfAbsentSuccess() {
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Boolean result = redisCache.multiSetIfAbsent(map);
        assertThat(result).isTrue();

        assertThat(redisCache.get("k1", String.class)).isEqualTo("v1");
        assertThat(redisCache.get("k2", String.class)).isEqualTo("v2");
    }

    @Test
    void testMultiSetIfAbsentPartialExist() {

        redisCache.set("k1", "v");
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        Boolean result = redisCache.multiSetIfAbsent(map);
        // 原子性失败
        assertThat(result).isFalse();
        // k1 保留原值，k2 不会被设置
        assertThat(redisCache.get("k1", String.class)).isEqualTo("v");
        assertThat(redisCache.get("k2", String.class)).isNull();
    }

    @Test
    void testMultiGet() {
        redisCache.set("k1", "value1");
        redisCache.set("k2", "value2");
        redisCache.set("k3", "value3");

        List<String> keys = Arrays.asList("k1", "k2", "k3");
        Map<String, String> result = redisCache.multiGet(keys, new TypeReference<String>() {});

        assertThat(result).hasSize(3);
        assertThat(result.get("k1")).isEqualTo("value1");
        assertThat(result.get("k2")).isEqualTo("value2");
        assertThat(result.get("k3")).isEqualTo("value3");

        redisCache.set("k4", null);
        List<String> key2 = Arrays.asList("k1", "k2", "k3", "k4");
        Map<String, String> result2 = redisCache.multiGet(key2, new TypeReference<>() {});
        assertThat(result2).hasSize(4);
        assertThat(result.get("k4")).isEqualTo(null);


        preventCachePenetrationRedisCache.set("k5", "value5");
        preventCachePenetrationRedisCache.set("k6", null);
        List<String> keysFromPrevent = Arrays.asList("k5", "k6");
        Map<String, String> result3 = preventCachePenetrationRedisCache.multiGet(keysFromPrevent, new TypeReference<>() {});
        assertThat(result3.get("k6")).isEqualTo(null);
    }




    @Test
    void testMultiGet_realRedis() {
        List<String> keys = Arrays.asList("k1", "k2");

        // 模拟加载函数
        Map<String, String> functionResult = new HashMap<>();
        functionResult.put("k1", "v1-from-db");
        functionResult.put("k2", "v2-from-db");

        // 第一次调用，缓存未命中，会走 function
        Map<String, String> result = redisCache.multiGet(
                keys,
                String.class,
                missedKeys -> functionResult,
                60,
                TimeUnit.SECONDS
        );

        assertEquals("v1-from-db", result.get("k1"));
        assertEquals("v2-from-db", result.get("k2"));

        // 第二次调用，应该从 Redis 命中，不走 function
        Map<String, String> cachedResult = redisCache.multiGet(
                keys,
                String.class,
                null,
                60,
                TimeUnit.SECONDS
        );

        assertEquals("v1-from-db", cachedResult.get("k1"));
        assertEquals("v2-from-db", cachedResult.get("k2"));


        //测试带缓存保护的
        Map<String, String> result2 = preventCachePenetrationRedisCache.multiGet(keys, String.class, missKeys -> null, 30, TimeUnit.MINUTES);

        Map<String, String> result3 = preventCachePenetrationRedisCache.multiGet(keys, String.class, missKeys -> {
            System.out.println("execute the result from functionResult");
            return functionResult;
        }, 30, TimeUnit.MINUTES);
        assertThat(result3.get("k1")).isEqualTo(null);
        assertEquals(null, preventCachePenetrationRedisCache.get("k1", String.class));

    }

    @Service
    public static class UserService {
        private final AtomicInteger callCount = new AtomicInteger(0);

        // 这里模拟实际业务：真实被锁保护的方法
        @RedisLock(key = "'userId:' + #user.userId", tryLockTime = 100)
        public void createUser(TestUser user) {
            callCount.incrementAndGet();
            System.out.println("执行 createUser: " + user.getUserId());
            try { Thread.sleep(2000); } catch (Exception ignored) {}
        }
        public int getCallCount() {
            return callCount.get();
        }
    }
}
