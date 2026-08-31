package org.jejuro.miraero.domain.youthpolicy.dto.response;

import java.time.LocalDate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "청년 정책 상세 정보")
public class YouthPolicyDetailResponse {

    @ApiModelProperty(value = "청년 정책 ID")
    private Long youthPolicyId;
    @ApiModelProperty(value = "정책명")
    private String policyName;
    @ApiModelProperty(value = "정책 키워드")
    private String policyKeyword;
    @ApiModelProperty(value = "정책 소개")
    private String policyDescription;
    @ApiModelProperty(value = "지원 내용")
    private String supportContent;
    @ApiModelProperty(value = "정책 제공 기관명")
    private String providerInstitutionName;
    @ApiModelProperty(value = "신청 시작일")
    private LocalDate applicationStartDate;
    @ApiModelProperty(value = "신청 마감일")
    private LocalDate applicationEndDate;
    @ApiModelProperty(value = "신청 기간 안내 문구")
    private String applicationPeriod;
    @ApiModelProperty(value = "지원 최소 연령")
    private Integer minAge;
    @ApiModelProperty(value = "지원 최대 연령")
    private Integer maxAge;
    @ApiModelProperty(value = "최소 소득 기준(원)")
    private Long minIncome;
    @ApiModelProperty(value = "최대 소득 기준(원)")
    private Long maxIncome;
    @ApiModelProperty(value = "소득 조건 상세 문구")
    private String incomeConditionText;
    @ApiModelProperty(value = "신청 자격")
    private String qualification;
    @ApiModelProperty(value = "신청 방법")
    private String applicationMethod;
    @ApiModelProperty(value = "신청 페이지 URL")
    private String applicationUrl;
    @ApiModelProperty(value = "참고 URL")
    private String referenceUrl;
}
