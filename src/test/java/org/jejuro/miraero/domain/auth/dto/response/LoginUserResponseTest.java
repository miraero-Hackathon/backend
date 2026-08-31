package org.jejuro.miraero.domain.auth.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.jejuro.miraero.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginUserResponseTest {

  @Test
  @DisplayName("연동 및 목표 설정이 모두 완료된 사용자")
  void from_connectedAndGoalSet() {
    LoginUserResponse response = LoginUserResponse.from(createUser(10001L), true);

    assertEquals(true, response.getMydataConnected());
    assertEquals(true, response.getGoalSet());
  }

  @Test
  @DisplayName("연동은 됐지만 목표를 아직 설정하지 않은 사용자")
  void from_connectedButGoalNotSet() {
    LoginUserResponse response = LoginUserResponse.from(createUser(10001L), false);

    assertEquals(true, response.getMydataConnected());
    assertEquals(false, response.getGoalSet());
  }

  @Test
  @DisplayName("연동되지 않은 사용자는 목표 설정 여부와 무관하게 mydataConnected가 false")
  void from_notConnected() {
    LoginUserResponse response = LoginUserResponse.from(createUser(null), true);

    assertEquals(false, response.getMydataConnected());
    assertEquals(true, response.getGoalSet());
  }

  @Test
  @DisplayName("연동도 안 됐고 목표도 설정하지 않은 사용자")
  void from_notConnectedAndGoalNotSet() {
    LoginUserResponse response = LoginUserResponse.from(createUser(null), false);

    assertEquals(false, response.getMydataConnected());
    assertEquals(false, response.getGoalSet());
  }

  private User createUser(Long kbPayId) {
    return User.create(
        "테스트 사용자",
        LocalDate.of(2000, 1, 1),
        "테스트 회사",
        3_000_000L,
        "test@example.com",
        "encodedPassword",
        kbPayId
    );
  }
}
