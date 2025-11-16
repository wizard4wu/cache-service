package com.dev.wizard.cache.config;

import com.dev.wizard.cache.redis.MyAbstractRedisCache;
import com.dev.wizard.cache.redis.MyRedisCache;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.TransportMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Author: wizard.wu
 * Date: 2025/9/6 09:10
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@ComponentScan("com.dev.wizard.cache")
@EnableAspectJAutoProxy
public class RedisAutoConfig {

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        //support cluster mode
        if(null != redisProperties.getCluster()  && !CollectionUtils.isEmpty(redisProperties.getCluster().getNodes())) {
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
            clusterConfig.setPassword(redisProperties.getPassword());
            return new LettuceConnectionFactory(clusterConfig);
        }
        //default standalone mode
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(redisProperties.getHost());
        standaloneConfig.setPort(redisProperties.getPort());
        standaloneConfig.setDatabase(redisProperties.getDatabase());
        standaloneConfig.setPassword(redisProperties.getPassword());
        return new LettuceConnectionFactory(standaloneConfig);
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate redisObjectTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jacksonSerializer);
        return template;
    }


    /**
     * 分布式锁服务，基于 Redisson 实现
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient createRedissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        config.setTransportMode(TransportMode.NIO); // 可选 NIO 或 EPOLL
        final String scheme = redisProperties.isSsl() ? "rediss" : "redis";

        //===== 集群模式 =====
        if (redisProperties.getCluster() != null &&
                redisProperties.getCluster().getNodes() != null &&
                !redisProperties.getCluster().getNodes().isEmpty()) {
            ClusterServersConfig clusterCfg = config.useClusterServers();
            List<String> nodeAddresses = redisProperties.getCluster().getNodes().stream()
                    .map(node -> scheme + "://" + node.trim())
                    .toList();
            clusterCfg.addNodeAddress(nodeAddresses.toArray(String[]::new));
            if(null != redisProperties.getPassword()){
                clusterCfg.setPassword(redisProperties.getPassword().trim());
            }
        } else {
            // ===== 单机模式 =====
            SingleServerConfig singleCfg = config.useSingleServer();
            String address = String.format("%s://%s:%d", scheme, redisProperties.getHost(), redisProperties.getPort());
            singleCfg.setAddress(address);
            if (redisProperties.getPassword() != null) {
                singleCfg.setPassword(redisProperties.getPassword().trim());
            }
            singleCfg.setDatabase(redisProperties.getDatabase());
        }
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }


    @Bean
    public MyRedisCache myCache(StringRedisTemplate redisObjectTemplate) {
      return new MyAbstractRedisCache.Default(redisObjectTemplate, "my", false);
    }
}
