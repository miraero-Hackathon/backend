package org.jejuro.miraero.domain.mydata.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyDataConsent {

  private Long userId;
  private String termsVersion;
  private LocalDateTime agreedAt;
  private LocalDateTime expiresAt;
  private LocalDateTime revokedAt;
  private String agreeStatus;

  public static MyDataConsent create(
      Long userId,
      String termsVersion,
      LocalDateTime agreedAt,
      LocalDateTime expiresAt,
      String agreeStatus
  ) {
    return new MyDataConsent(
        userId,
        termsVersion,
        agreedAt,
        expiresAt,
        null,
        agreeStatus
    );
  }
}
