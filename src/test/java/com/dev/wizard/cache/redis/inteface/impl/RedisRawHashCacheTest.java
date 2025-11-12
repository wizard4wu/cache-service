package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.config.RedisAutoConfig;
import com.dev.wizard.cache.domain.TestUser;
import com.dev.wizard.cache.redis.MyRedisCache;
import com.dev.wizard.cache.redis.anno.RedisCache;
import com.dev.wizard.cache.redis.inteface.RedisRawHashCache;
import com.dev.wizard.cache.redis.inteface.RedisRawListCache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Author: wizard.wu
 * Date: 2025/11/9 21:54
 */
@SpringBootTest(classes = RedisAutoConfig.class)
public class RedisRawHashCacheTest {

    @RedisCache("test")
    private MyRedisCache redisCache;

    private RedisRawHashCache<String, TestUser> hashCache;

    @PostConstruct
    public void init() {
        hashCache = redisCache.hashCache(String.class, TestUser.class);
    }
    @Test
    void testAllOperations() {
        String key = "user:hash:test";

        redisCache.delete(key);
        // 测试数据
        TestUser user1 = new TestUser(1L, "Alice", Instant.now());
        TestUser user2 = new TestUser(2L, "Bob", Instant.now());

        // put
        hashCache.put(key, String.valueOf(user1.getUserId()), user1);
        hashCache.put(key, String.valueOf(user2.getUserId()), user2);

        // size
        Long size = hashCache.size(key);
        assertEquals(2L, size);

        // hasKey
        assertTrue(hashCache.hasKey(key, "1"));
        assertFalse(hashCache.hasKey(key, "3"));

        // get
        TestUser u1 = hashCache.get(key, "1");
        assertNotNull(u1);
        assertEquals("Alice", u1.getUserName());

        // multiGet
        List<TestUser> users = hashCache.multiGet(key, Arrays.asList("1", "2", "3"));
        assertEquals(2, users.size());

        // keys
        Set<String> keySet = hashCache.keys(key);
        assertTrue(keySet.containsAll(Arrays.asList("1", "2")));

        // putIfAbsent
        Boolean absent = hashCache.putIfAbsent(key, "3", new TestUser(3L, "Charlie", Instant.now()));
        assertTrue(absent);

        Boolean exist = hashCache.putIfAbsent(key, "1", new TestUser(1L, "Other", Instant.now()));
        assertFalse(exist);

        // delete
        Long delCount = hashCache.delete(key, "1", "2");
        assertEquals(2L, delCount);

        // 最终 key 数量为 1
        assertEquals(1L, hashCache.size(key));
    }
}
