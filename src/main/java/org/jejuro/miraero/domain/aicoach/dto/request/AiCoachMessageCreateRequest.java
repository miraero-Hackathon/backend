package org.jejuro.miraero.domain.aicoach.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;

@Getter
@ApiModel(description = "AI 코치 질문 요청")
public class AiCoachMessageCreateRequest {

    @ApiModelProperty(value = "AI 코치에게 보낼 질문 내용", required = true, example = "이번 달 식비를 줄이려면 어떻게 해야 할까요?")
    @NotBlank
    @Size(max = 1000)
    private String content;
}
