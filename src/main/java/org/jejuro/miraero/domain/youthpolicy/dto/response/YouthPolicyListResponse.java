package org.jejuro.miraero.domain.youthpolicy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "청년 정책 목록 항목")
public class YouthPolicyListResponse {

    @ApiModelProperty(value = "청년 정책 ID", example = "1")
    private Long youthPolicyId;
    @ApiModelProperty(value = "정책명")
    private String policyName;
    @ApiModelProperty(value = "정책 키워드")
    private String policyKeyword;
    @ApiModelProperty(value = "정책 제공 기관명")
    private String providerInstitutionName;
    @ApiModelProperty(value = "신청 기간 안내 문구")
    private String applicationPeriod;
    @Getter(AccessLevel.NONE)
    private Long dDay;

    @JsonProperty("dDay")
    @ApiModelProperty(value = "신청 마감일까지 남은 일수. 마감된 경우 음수일 수 있음", example = "12")
    public Long getDDay() {
        return dDay;
    }
}
