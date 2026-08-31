package org.jejuro.miraero.domain.product.dto.response;

import java.time.LocalDate;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "예금 상품 상세 정보")
public class DepositProductDetailResponse {

    @ApiModelProperty(value = "예금 상품 ID", example = "1")
    private Long depositProductId;
    @ApiModelProperty(value = "금융회사명")
    private String financialInstitutionName;
    @ApiModelProperty(value = "상품명")
    private String productName;
    @ApiModelProperty(value = "가입 방법")
    private String joinMethod;
    @ApiModelProperty(value = "가입 대상")
    private String joinTarget;
    @ApiModelProperty(value = "가입 제한 내용")
    private String joinRestriction;
    @ApiModelProperty(value = "가입 제한 여부")
    private Boolean hasJoinRestriction;
    @ApiModelProperty(value = "우대 조건")
    private String specialCondition;
    @ApiModelProperty(value = "만기 후 이자율 안내")
    private String maturityInterest;
    @ApiModelProperty(value = "최고 가입 한도(원)")
    private Long maxLimit;
    @ApiModelProperty(value = "유의사항")
    private String notice;
    @ApiModelProperty(value = "공시 기준 월(YYYYMM)")
    private String disclosureMonth;
    @ApiModelProperty(value = "공시 시작일")
    private LocalDate disclosureStartDate;
    @ApiModelProperty(value = "공시 종료일")
    private LocalDate disclosureEndDate;
    @ApiModelProperty(value = "금융회사 상품 상세 페이지 URL")
    private String productPageUrl;
    @ApiModelProperty(value = "기간별 금리 옵션")
    private List<DepositOptionResponse> options;
}
