package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.config.RedisAutoConfig;
import com.dev.wizard.cache.domain.TestUser;
import com.dev.wizard.cache.redis.MyRedisCache;
import com.dev.wizard.cache.redis.anno.RedisCache;
import com.dev.wizard.cache.redis.inteface.RedisRawSetCache;
import com.dev.wizard.cache.redis.inteface.RedisRawSortedSetCache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Author: wizard.wu
 * Date: 2025/11/9 22:49
 */
@SpringBootTest(classes = RedisAutoConfig.class)
public class RedisRwaSortedSetCacheTest {


    @RedisCache(value = "test1")
    private MyRedisCache redisCache;

    private RedisRawSortedSetCache<TestUser> sortedSetCache;
    @PostConstruct
    public void init() {
        sortedSetCache = redisCache.sortedSetCache(TestUser.class);
    }

    @Test
    void testZSetCacheOperations() {
        String key = "test:zset:users";

        // 清空旧数据
        redisCache.delete(key);

        // 构造测试数据
        TestUser user1 = new TestUser(1L, "Tom", Instant.now());
        TestUser user2 = new TestUser(2L, "Jerry", Instant.now());
        TestUser user3 = new TestUser(3L, "Spike", Instant.now());

        // 1️⃣ add
        Boolean added1 = sortedSetCache.add(key, user1, 10.0);
        Boolean added2 = sortedSetCache.add(key, user2, 20.0);
        Boolean added3 = sortedSetCache.add(key, user3, 30.0);
        assertTrue(added1);
        assertTrue(added2);
        assertTrue(added3);

        // 2️⃣ size
        Long size = sortedSetCache.size(key);
        assertEquals(3L, size);

        // 3️⃣ addIfAbsent (重复添加 user1 不应影响集合)
        Boolean absentAdd = sortedSetCache.addIfAbsent(key, user1, 99.0);
        assertFalse(absentAdd);
        assertEquals(3L, sortedSetCache.size(key));

        // 4️⃣ rangeByScore
        Set<TestUser> range = sortedSetCache.rangeByScore(key, 10.0, 20.0);
        assertEquals(2, range.size());
        assertTrue(range.stream().anyMatch(u -> "Tom".equals(u.getUserName())));
        assertTrue(range.stream().anyMatch(u -> "Jerry".equals(u.getUserName())));

        // 5️⃣ remove
        Long removedCount = sortedSetCache.remove(key, user2);
        assertEquals(1L, removedCount);

        // 再次检查 size
        Long newSize = sortedSetCache.size(key);
        assertEquals(2L, newSize);

        // 验证剩下的元素
        Set<TestUser> remaining = sortedSetCache.rangeByScore(key, 0, 100);
        assertEquals(2, remaining.size());
        assertTrue(remaining.stream().anyMatch(u -> "Tom".equals(u.getUserName())));
        assertTrue(remaining.stream().anyMatch(u -> "Spike".equals(u.getUserName())));
    }
}
