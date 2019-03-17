package com.spring.redis.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;
import java.util.Collections;

@Configuration
@EnableCaching
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.session-cache.ttl}")
    private int sessionTimeout;

    @Autowired
    RedisClusterConfigurationProperties clusterProperties;

    @Profile("dev")
    @Bean(name = "redisConfig")
    public RedisConnectionFactory devRedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().build();
        JedisConnectionFactory factory = new JedisConnectionFactory(configuration, jedisClientConfiguration);
        factory.afterPropertiesSet();
        return factory;
    }

    @Profile("production")
    @Bean(name = "redisConfig")
    public RedisConnectionFactory prodRedisConnectionFactory() {
        return new JedisConnectionFactory(
                new RedisClusterConfiguration(clusterProperties.getNodes()));
    }

    @Primary
    @Bean(name = "sessionCacheManager")
    public RedisCacheManager sessionCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration sessionCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(sessionTimeout))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(sessionCacheConfig)
                .withInitialCacheConfigurations(Collections.singletonMap("session-cache", sessionCacheConfig))
                .build();
    }

    @Bean(name = "optionsCacheManager")
    public RedisCacheManager optionsCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration sessionCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(sessionCacheConfig)
                .withInitialCacheConfigurations(Collections.singletonMap("options-cache", sessionCacheConfig))
                .build();
    }

}