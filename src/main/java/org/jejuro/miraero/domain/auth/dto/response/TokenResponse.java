package org.jejuro.miraero.domain.auth.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "인증 토큰 정보")
public class TokenResponse {

  @ApiModelProperty(value = "인증 API 호출에 사용할 Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
  private String accessToken;
  @ApiModelProperty(value = "Authorization 헤더 토큰 유형", example = "Bearer")
  private String tokenType;
  @ApiModelProperty(value = "Access Token 만료까지 남은 시간(초)", example = "1800")
  private Long accessTokenExpiresIn;

}
