package org.jejuro.miraero.domain.product.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepositProductDetailQueryResult {

    private Long depositProductId;
    private String financialInstitutionName;
    private String productName;
    private String joinMethod;
    private String joinTarget;
    private String joinRestriction;
    private Boolean hasJoinRestriction;
    private String specialCondition;
    private String maturityInterest;
    private Long maxLimit;
    private String notice;
    private String disclosureMonth;
    private LocalDate disclosureStartDate;
    private LocalDate disclosureEndDate;
    private String productPageUrl;
}
