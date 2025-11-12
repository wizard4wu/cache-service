package com.dev.wizard.cache.redis.inteface;

import java.util.Collection;
import java.util.List;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:40
 */
public interface RedisRawListCache<V> {


    // -------------------- 添加元素 --------------------
    /**
     * 在列表左侧添加一个元素
     * Redis 命令: LPUSH
     */
    Long leftPush(String key, V value);

    /**
     * 在列表右侧添加一个元素
     * Redis 命令: RPUSH
     */
    Long rightPush(String key, V value);


    /**
     * 右侧批量添加元素
     * Redis 命令: RPUSH
     */
    Long rightPushAll(String key, Collection<V> values);

    /**
     * 仅当列表存在时才在左侧添加元素
     * Redis 命令: LPUSHX
     */
    Long leftPushIfPresent(String key, V value);

    /**
     * 仅当列表存在时才在右侧添加元素
     * Redis 命令: RPUSHX
     */
    Long rightPushIfPresent(String key, V value);

    // -------------------- 读取元素 --------------------

    /**
     * 获取指定下标的元素（0 为左侧第一个）
     * Redis 命令: LINDEX
     */
    V index(String key, long index);

    /**
     * 获取列表指定区间元素
     * start, end 支持负数（-1 为最后一个元素）
     * Redis 命令: LRANGE
     */
    List<V> range(String key, long start, long end);

    /**
     * 获取列表长度
     * Redis 命令: LLEN
     */
    Long size(String key);


    V rightPop(String key);

    V leftPop(String key);
}
