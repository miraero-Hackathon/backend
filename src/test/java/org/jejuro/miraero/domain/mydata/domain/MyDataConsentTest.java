package org.jejuro.miraero.domain.mydata.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MyDataConsentTest {

  @Test
  @DisplayName("마이데이터 동의 정보를 입력하면 동의 객체를 생성한다")
  void createMyDataConsentTest() {
    // given
    Long userId = 1L;
    String termsVersion = "v1.0";
    LocalDateTime agreedAt = LocalDateTime.of(2026, 7, 29, 10, 0);
    LocalDateTime expiresAt = agreedAt.plusYears(1);
    String agreeStatus = "AGREED";

    // when
    MyDataConsent myDataConsent = MyDataConsent.create(
        userId,
        termsVersion,
        agreedAt,
        expiresAt,
        agreeStatus
    );

    // then
    assertEquals(userId, myDataConsent.getUserId());
    assertEquals(termsVersion, myDataConsent.getTermsVersion());
    assertEquals(agreedAt, myDataConsent.getAgreedAt());
    assertEquals(expiresAt, myDataConsent.getExpiresAt());
    assertNull(myDataConsent.getRevokedAt());
    assertEquals(agreeStatus, myDataConsent.getAgreeStatus());
  }
}
