package org.jejuro.miraero.domain.auth.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.user.domain.User;

@Getter
@AllArgsConstructor
@ApiModel(description = "로그인 사용자 정보")
public class LoginUserResponse {

  @ApiModelProperty(value = "사용자 ID", example = "1")
  private Long userId;
  @ApiModelProperty(value = "사용자명")
  private String name;
  @ApiModelProperty(value = "이메일")
  private String email;
  @ApiModelProperty(value = "마이데이터 연결 여부")
  private Boolean mydataConnected;
  @ApiModelProperty(value = "활성 목표 설정 여부")
  private Boolean goalSet;

  public static LoginUserResponse from(User user, boolean goalSet) {
    return new LoginUserResponse(
        user.getUserId(),
        user.getName(),
        user.getEmail(),
        user.getKbPayId() != null,
        goalSet
    );
  }
}
