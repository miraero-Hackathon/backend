package org.jejuro.miraero.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

@Configuration
public class RedisConfig {

  @Value("${redis.host}")
  private String redisHost;

  @Value("${redis.port}")
  private int redisPort;

  @Value("${redis.password:}")
  private String redisPassword;

  @Value("${redis.database}")
  private int redisDatabase;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisConfig =
        new RedisStandaloneConfiguration(redisHost, redisPort);

    redisConfig.setDatabase(redisDatabase);

    if (StringUtils.hasText(redisPassword)) {
      redisConfig.setPassword(RedisPassword.of(redisPassword));
    }

    return new LettuceConnectionFactory(redisConfig);
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate(
      RedisConnectionFactory redisConnectionFactory
  ) {
    return new StringRedisTemplate(redisConnectionFactory);
  }
}
