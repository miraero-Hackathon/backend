package org.jejuro.miraero.domain.mydata.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisMyDataTokenRepository implements MyDataTokenRepository {

  private final StringRedisTemplate stringRedisTemplate;

  @Value("${redis.key-prefix.mydata-token}")
  private String myDataTokenKeyPrefix;

  @Override
  public void save(Long userId, String accessToken, Long expiresIn) {
    // TTL을 실제 토큰 만료 시각에 맞춰 만료된 토큰이 Redis에 남지 않도록 함
    stringRedisTemplate.opsForValue().set(
        createKey(userId),
        accessToken,
        Duration.ofSeconds(expiresIn)
    );
  }

  @Override
  public String findByUserId(Long userId) {
    return stringRedisTemplate.opsForValue().get(createKey(userId));
  }

  @Override
  public void deleteByUserId(Long userId) {
    stringRedisTemplate.delete(createKey(userId));
  }

  private String createKey(Long userId) {
    return myDataTokenKeyPrefix + userId;
  }
}
