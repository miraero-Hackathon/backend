package org.jejuro.miraero.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel(description = "Access Token 재발급 응답")
public class TokenReissueResponse {

  @ApiModelProperty(value = "새 Access Token 정보")
  private final TokenResponse token;
  @ApiModelProperty(value = "로그인 사용자 정보")
  private final LoginUserResponse user;

  @JsonIgnore
  private final String refreshToken;

  @JsonIgnore
  private final Long refreshTokenExpiresIn;

  public TokenReissueResponse(
      String accessToken,
      String refreshToken,
      Long accessTokenExpiresIn,
      Long refreshTokenExpiresIn,
      LoginUserResponse user
  ) {
    this.token = new TokenResponse(
        accessToken,
        "Bearer",
        accessTokenExpiresIn
    );
    this.user = user;
    this.refreshToken = refreshToken;
    this.refreshTokenExpiresIn = refreshTokenExpiresIn;
  }
}
