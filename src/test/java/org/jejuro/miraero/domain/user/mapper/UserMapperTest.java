package org.jejuro.miraero.domain.user.mapper;


import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.global.config.DataSourceConfig;
import org.jejuro.miraero.global.config.MyBatisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DataSourceConfig.class,
    MyBatisConfig.class
})

@Transactional
public class UserMapperTest {

  @Autowired
  private UserMapper userMapper;

  @Test
  @DisplayName("회원을 저장하고 이메일 존재 여부를 확인한다")
  void saveAndExistsByEmail() {
    // given
    User user = User.create(
        "홍길동",
        LocalDate.of(2000, 1, 1),
        "미래로",
        3_000_000L,
        "mapper-test@example.com",
        "encodedPassword",
        100L
    );

    // when
    int result = userMapper.save(user);
    boolean exists = userMapper.existsByEmail(user.getEmail());

    // then
    assertEquals(1, result);
    assertNotNull(user.getUserId());
    assertTrue(exists);
  }

  @Test
  @DisplayName("이메일로 회원을 조회한다.")
  void findByEmail() {
    //given
    User user = User.create(
        "테스트 사용자",
        LocalDate.of(2000, 1, 1),
        "테스트 회사",
        3_000_000L,
        "login-test@example.com",
        "encodedPassword",
        1L
    );

    userMapper.save(user);

    //when
    User foundUser = userMapper.findByEmail("login-test@example.com");

    //then
    assertNotNull(foundUser);
    assertEquals(user.getUserId(), foundUser.getUserId());
    assertEquals("테스트 사용자", foundUser.getName());
    assertEquals("login-test@example.com", foundUser.getEmail());
  }

  @Test
  @DisplayName("존재하지 않는 이메일이면 null을 반환한다")
  void findByEmailNull() {
    //when
    User foundUser = userMapper.findByEmail("not-found@example.com");

    //then
    assertNull(foundUser);
  }

  @Test
  @DisplayName("마이데이터 연동 시 회원가입 목업 값을 본인확인 정보로 덮어쓴다")
  void updateProfile() {
    User user = User.create(
        "테스트 사용자",
        LocalDate.of(2000, 1, 1),
        "테스트 회사",
        3_000_000L,
        "profile-test@example.com",
        "encodedPassword",
        null
    );
    userMapper.save(user);

    int updated = userMapper.updateProfile(
        user.getUserId(), "탁민주", LocalDate.of(1999, 4, 18), "중견기업J", 2_850_000L);

    assertEquals(1, updated);
    User found = userMapper.findByEmail("profile-test@example.com");
    assertEquals("탁민주", found.getName());
    assertEquals(LocalDate.of(1999, 4, 18), found.getBirthDate());
    assertEquals("중견기업J", found.getCompanyName());
    assertEquals(2_850_000L, found.getMonthlyIncome());
  }
}
