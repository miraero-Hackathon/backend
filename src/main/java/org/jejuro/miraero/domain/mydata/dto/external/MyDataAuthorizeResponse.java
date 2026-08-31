package org.jejuro.miraero.domain.mydata.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyDataAuthorizeResponse {

  private String authorizationCode;
  private Long expiresIn;
}
