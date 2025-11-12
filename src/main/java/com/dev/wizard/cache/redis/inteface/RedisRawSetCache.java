package com.dev.wizard.cache.redis.inteface;

import org.springframework.lang.Nullable;
import java.util.Set;

/**
 * Author: wizard.wu
 * Date: 2025/10/23 23:41
 */
public interface RedisRawSetCache<V>{

    /**
     * 向集合中添加一个或多个元素
     * @param key Redis key（建议使用业务前缀）
     * @param value 元素
     * @return 成功添加的数量
     */
    Long add(String key, V value);

    /**
     * 获取集合中的所有元素
     * @param key Redis key
     * @return 元素集合
     */
    Set<V> members(String key);

    /**
     * 判断指定元素是否在集合中
     * @param key Redis key
     * @param value 要判断的元素
     * @return true：存在，false：不存在
     */
    Boolean isMember(String key, V value);

    /**
     * 从集合中移除指定元素
     * @param key Redis key
     * @param value 要移除的元素
     * @return 实际移除的数量
     */
    Long remove(String key, V value);

    /**
     * 获取集合中元素数量
     * @param key Redis key
     * @return 集合大小
     */
    @Nullable
    Long size(String key);

}
