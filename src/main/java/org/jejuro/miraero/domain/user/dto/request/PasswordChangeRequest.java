package org.jejuro.miraero.domain.user.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
@ApiModel(description = "비밀번호 변경 요청")
public class PasswordChangeRequest {
  @ApiModelProperty(value = "현재 비밀번호", required = true)
  @NotBlank(message = "Current password is required.") private String currentPassword;
  @ApiModelProperty(value = "새 비밀번호(8자 이상)", required = true, example = "newPassword123!")
  @NotBlank(message = "New password is required.")
  @Size(min = 8, message = "New password must be at least 8 characters.") private String newPassword;
  @ApiModelProperty(value = "새 비밀번호 확인", required = true, example = "newPassword123!")
  @NotBlank(message = "New password confirmation is required.") private String newPasswordConfirm;
}
