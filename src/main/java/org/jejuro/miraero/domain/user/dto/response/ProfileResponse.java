package org.jejuro.miraero.domain.user.dto.response;

import lombok.AllArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "내 프로필 정보")
public class ProfileResponse {

  @ApiModelProperty(value = "사용자 ID")
  private Long userId;
  @ApiModelProperty(value = "사용자명")
  private String name;
  @ApiModelProperty(value = "이메일")
  private String email;
  @ApiModelProperty(value = "생년월일 문자열")
  private String birthDate;
  @ApiModelProperty(value = "프로필 이미지 URL")
  private String profileImageUrl;
  @ApiModelProperty(value = "직장 또는 소속")
  private String company;
  @ApiModelProperty(value = "월 소득(원)")
  private Long monthlyIncome;
  @ApiModelProperty(value = "KB Pay 연동 여부")
  private boolean kbpayLinked;
}
