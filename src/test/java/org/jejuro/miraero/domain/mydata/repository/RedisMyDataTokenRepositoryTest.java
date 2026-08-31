package org.jejuro.miraero.domain.mydata.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RedisMyDataTokenRepositoryTest {

  private static final Long USER_ID = 7L;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private RedisMyDataTokenRepository repository;

  @BeforeEach
  void setUp() {
    repository = new RedisMyDataTokenRepository(stringRedisTemplate);
    ReflectionTestUtils.setField(repository, "myDataTokenKeyPrefix", "mydata:token:");
  }

  @Test
  @DisplayName("토큰을 TTL과 함께 저장한다")
  void save() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

    repository.save(USER_ID, "token-1", 3600L);

    verify(valueOperations).set(eq("mydata:token:7"), eq("token-1"), eq(Duration.ofSeconds(3600L)));
  }

  @Test
  @DisplayName("사용자 ID로 토큰을 조회한다")
  void findByUserId() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("mydata:token:7")).thenReturn("token-1");

    assertEquals("token-1", repository.findByUserId(USER_ID));
  }
}
