package org.jejuro.miraero.domain.user.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
//개발자가 new User로 생성하는 경우 방지
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

  private Long userId;
  private String name;
  private LocalDate birthDate;
  private String companyName;
  private Long monthlyIncome;
  private String email;
  private String passwordHash;
  private Long kbPayId;
  private LocalDateTime createdAt;

  public static User create(
      String name,
      LocalDate birthDate,
      String companyName,
      Long monthlyIncome,
      String email,
      String passwordHash,
      Long kbPayId
  ) {
    return new User(
        null,
        name,
        birthDate,
        companyName,
        monthlyIncome,
        email,
        passwordHash,
        kbPayId,
        null
    );
  }

}
