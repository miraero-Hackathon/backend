package org.jejuro.miraero.domain.auth.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

  private final StringRedisTemplate stringRedisTemplate;

  @Value("${redis.key-prefix.refresh-token}")
  private String refreshTokenKeyPrefix;

  @Override
  public void save(Long userId, String refreshToken, Long expiresIn) {
    stringRedisTemplate.opsForValue().set(
        createKey(userId),
        refreshToken,
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
    return refreshTokenKeyPrefix + userId;
  }
}
