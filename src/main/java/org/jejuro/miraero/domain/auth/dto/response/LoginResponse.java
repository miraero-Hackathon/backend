package org.jejuro.miraero.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel(description = "로그인 응답")
public class LoginResponse {

  @ApiModelProperty(value = "Access Token 정보")
  private final TokenResponse token;
  @ApiModelProperty(value = "자동 로그인 적용 여부")
  private final Boolean autoLogin;
  @ApiModelProperty(value = "로그인 사용자 정보")
  private final LoginUserResponse user;

  @JsonIgnore
  private final String refreshToken;

  @JsonIgnore
  private final Long refreshTokenExpiresIn;

  public LoginResponse(
      String accessToken,
      String refreshToken,
      Long accessTokenExpiresIn,
      Long refreshTokenExpiresIn,
      Boolean autoLogin,
      LoginUserResponse user
  ) {
    this.token = new TokenResponse(
        accessToken,
        "Bearer",
        accessTokenExpiresIn
    );
    this.autoLogin = autoLogin;
    this.user = user;
    this.refreshToken = refreshToken;
    this.refreshTokenExpiresIn = refreshTokenExpiresIn;
  }
}
