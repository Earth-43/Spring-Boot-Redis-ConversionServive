package com.spring.redis.configuration;

import com.spring.redis.cache.UserCacheKey;
import com.spring.redis.cache.UserCacheKeyConverter;
import com.spring.redis.service.UserService;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.format.support.DefaultFormattingConversionService;

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

    @Bean(name = "userCacheManager")
    public RedisCacheManager userCacheManager(RedisConnectionFactory connectionFactory, UserService userService) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        DefaultFormattingConversionService conversionService = (DefaultFormattingConversionService) redisCacheConfiguration.getConversionService();
        conversionService.addConverter(UserCacheKey.class, String.class, new UserCacheKeyConverter(userService));
        redisCacheConfiguration
                .entryTtl(Duration.ofSeconds(600)).withConversionService(conversionService)
                .disableCachingNullValues();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(Collections.singletonMap("user-cache", redisCacheConfiguration))
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

}