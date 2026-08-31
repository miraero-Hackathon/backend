package org.jejuro.miraero.domain.product.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SavingProductListQueryResult {

    private Long savingProductId;
    private String financialInstitutionName;
    private String productName;
    private BigDecimal highestInterestRate;
    private Integer saveTerm;
    private String reserveType;
    private Long maxLimit;
    private String joinMethod;
    private Boolean hasJoinRestriction;
    private Boolean hasSpecialCondition;
}
