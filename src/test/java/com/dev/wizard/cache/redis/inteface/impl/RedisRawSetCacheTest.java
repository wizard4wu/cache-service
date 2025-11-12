package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.config.RedisAutoConfig;
import com.dev.wizard.cache.domain.TestUser;
import com.dev.wizard.cache.redis.MyRedisCache;
import com.dev.wizard.cache.redis.anno.RedisCache;
import com.dev.wizard.cache.redis.inteface.RedisRawHashCache;
import com.dev.wizard.cache.redis.inteface.RedisRawSetCache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author: wizard.wu
 * Date: 2025/11/9 22:35
 */
@SpringBootTest(classes = RedisAutoConfig.class)
public class RedisRawSetCacheTest {

    @RedisCache(value = "test1")
    private MyRedisCache redisCache;

    private RedisRawSetCache<TestUser> setCache;
    @PostConstruct
    public void init() {
        setCache = redisCache.setCache(TestUser.class);
    }


    @Test
    void testSetCacheOperations() {
      

        String key = "test:set:users";
        // 清理旧数据
        redisCache.delete(key);

        // 构造测试数据
        TestUser user1 = new TestUser(1L, "Tom", Instant.now());
        TestUser user2 = new TestUser(2L, "Jerry", Instant.now());

        // add
        Long added1 = setCache.add(key, user1);
        Long added2 = setCache.add(key, user2);
        assertTrue(added1 > 0);
        assertTrue(added2 > 0);

        // size
        Long size = setCache.size(key);
        assertEquals(2L, size);

        // members
        Set<TestUser> members = setCache.members(key);
        assertEquals(2, members.size());
        assertTrue(members.stream().anyMatch(u -> "Tom".equals(u.getUserName())));
        assertTrue(members.stream().anyMatch(u -> "Jerry".equals(u.getUserName())));

        // isMember
        assertTrue(setCache.isMember(key, user1));
        assertTrue(setCache.isMember(key, user2));

        // remove
        Long removed = setCache.remove(key, user1);
        assertEquals(1L, removed);

        // 再次检查 size
        Long newSize = setCache.size(key);
        assertEquals(1L, newSize);

        // 剩下的成员验证
        Set<TestUser> afterRemove = setCache.members(key);
        assertEquals(1, afterRemove.size());
        assertTrue(afterRemove.stream().anyMatch(u -> "Jerry".equals(u.getUserName())));
    }
}
