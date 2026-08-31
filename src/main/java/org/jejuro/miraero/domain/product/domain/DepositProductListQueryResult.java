package org.jejuro.miraero.domain.product.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepositProductListQueryResult {

    private Long depositProductId;
    private String financialInstitutionName;
    private String productName;
    private BigDecimal maxInterestRate;
    private Integer saveTerm;
    private Long maxLimit;
    private String joinMethod;
    private Boolean hasJoinRestriction;
    private Boolean hasSpecialCondition;
}
