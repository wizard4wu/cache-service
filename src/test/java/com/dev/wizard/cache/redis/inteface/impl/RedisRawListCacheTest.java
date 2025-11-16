package com.dev.wizard.cache.redis.inteface.impl;

import com.dev.wizard.cache.config.RedisAutoConfig;
import com.dev.wizard.cache.domain.TestUser;
import com.dev.wizard.cache.redis.MyRedisCache;
import com.dev.wizard.cache.redis.anno.RedisCache;
import com.dev.wizard.cache.redis.inteface.RedisRawListCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Author: wizard.wu
 * Date: 2025/11/9 15:54
 */
@SpringBootTest(classes = RedisAutoConfig.class)
public class RedisRawListCacheTest {

    @RedisCache("test")
    private MyRedisCache redisCache;

    private RedisRawListCache<TestUser> listCache;
    @PostConstruct
    public void init() {
        listCache = redisCache.listCache(TestUser.class);
    }
    @Test
    public void test() {
        String key = "user";
        redisCache.delete(key);

        TestUser user1 = new TestUser(11111L, "bob", Instant.now());
        Long result1 = listCache.leftPushIfPresent(key, user1);
        Long result2 = listCache.rightPushIfPresent(key, user1);
        Assertions.assertEquals(0, result1);
        Assertions.assertEquals(0, result2);


        Long result3 = listCache.leftPush(key, user1);
        Assertions.assertEquals(1, result3);

        TestUser user2 = new TestUser(22222L, "Jerry", Instant.now());
        Long result4 = listCache.leftPush(key, user2);
        Assertions.assertEquals(2, result4);


        TestUser user3 = new TestUser(3333L, "Tom", Instant.now());
        Long result5 = listCache.rightPush(key, user3);
        Assertions.assertEquals(3, result5);


        TestUser user4 = new TestUser(4444L, "Tom4", Instant.now());
        TestUser user5 = new TestUser(5555L, "Tom5", Instant.now());
        List<TestUser> list = Arrays.asList(user4, user5);
        Long result6 = listCache.rightPushAll(key, list);
        Assertions.assertEquals(5, result6);

        TestUser result7 = listCache.index(key, 5);
        Assertions.assertNull(result7);

        TestUser result8 = listCache.index(key, 4);
        Assertions.assertNotNull(result8);


        List<TestUser> result9 = listCache.range(key, 0, 3);
        Assertions.assertEquals(4, result9.size());

        TestUser leftUser = listCache.leftPop(key);
        Assertions.assertEquals(user2, leftUser);

        TestUser rightUser = listCache.rightPop(key);
        Assertions.assertEquals(user5, rightUser);
    }

}
