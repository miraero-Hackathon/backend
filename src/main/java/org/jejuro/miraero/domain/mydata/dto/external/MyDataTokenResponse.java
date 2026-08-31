package org.jejuro.miraero.domain.mydata.dto.external;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyDataTokenResponse {

  private String accessToken;
  private Long expiresIn;
  private Long kbUserId;

  // 마이데이터 본인확인 정보. 목서버가 토큰 응답에 함께 실어 보낸다
  private String name;
  private LocalDate birthDate;
  private Long monthlyIncome;
  private String companyName;
}
