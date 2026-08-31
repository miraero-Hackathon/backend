package org.jejuro.miraero.domain.auth.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.user.domain.User;

@Getter
@AllArgsConstructor
@ApiModel(description = "회원가입 완료 사용자 정보")
public class SignUpResponse {

    @ApiModelProperty(value = "사용자 ID", example = "1")
    private Long userId;
    @ApiModelProperty(value = "사용자명")
    private String name;
    @ApiModelProperty(value = "이메일", example = "user@example.com")
    private String email;

    public static SignUpResponse from(User user) {
        return new SignUpResponse(
            user.getUserId(),
            user.getName(),
            user.getEmail()
        );
    }
}
