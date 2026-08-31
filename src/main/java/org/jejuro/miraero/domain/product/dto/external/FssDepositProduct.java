package org.jejuro.miraero.domain.product.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FssDepositProduct {

    @JsonProperty("dcls_month")
    private String disclosureMonth;

    @JsonProperty("fin_co_no")
    private String financialCompanyCode;

    @JsonProperty("fin_prdt_cd")
    private String financialProductCode;

    @JsonProperty("kor_co_nm")
    private String financialCompanyName;

    @JsonProperty("fin_prdt_nm")
    private String financialProductName;

    @JsonProperty("join_way")
    private String joinWay;

    @JsonProperty("mtrt_int")
    private String maturityInterest;

    @JsonProperty("spcl_cnd")
    private String specialCondition;

    @JsonProperty("join_deny")
    private String joinDeny;

    @JsonProperty("join_member")
    private String joinMember;

    @JsonProperty("etc_note")
    private String etcNote;

    @JsonProperty("max_limit")
    private Long maxLimit;

    @JsonProperty("dcls_strt_day")
    private String disclosureStartDay;

    @JsonProperty("dcls_end_day")
    private String disclosureEndDay;

    @JsonProperty("fin_co_subm_day")
    private String financialCompanySubmittedDay;
}
