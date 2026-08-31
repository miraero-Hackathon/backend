package org.jejuro.miraero.domain.user.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  @DisplayName("회원 정보를 입력하면 신규 회원 객체를 생성한다")
  void createUserTest() {
    //given
    String name = "홍길동";
    LocalDate birthDate = LocalDate.of(2000, 1, 1);
    String companyName = "미래로";
    Long monthlyIncome = 3_000_000L;
    String email = "test@example.com";
    String passwordHash = "encodedPassword";
    Long kbPayId = 100L;

    //when
    User user = User.create(
        name,
        birthDate,
        companyName,
        monthlyIncome,
        email,
        passwordHash,
        kbPayId
    );

    //then
    assertNull(user.getUserId());
    assertEquals(name, user.getName());
    assertEquals(birthDate, user.getBirthDate());
    assertEquals(companyName, user.getCompanyName());
    assertEquals(monthlyIncome, user.getMonthlyIncome());
    assertEquals(email, user.getEmail());
    assertEquals(passwordHash, user.getPasswordHash());
    assertEquals(kbPayId, user.getKbPayId());
    assertNull(user.getCreatedAt());
  }

}
