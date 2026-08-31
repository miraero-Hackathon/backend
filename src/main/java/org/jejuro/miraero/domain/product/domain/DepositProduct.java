package org.jejuro.miraero.domain.product.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositProduct {

    private Long depositProductId;
    private Long financialInstitutionId;
    private String productCode;
    private String productName;
    private String joinMethod;
    private String joinTarget;
    private String joinRestriction;
    private String specialCondition;
    private String maturityInterest;
    private Long maxLimit;
    private String notice;
    private String disclosureMonth;
    private LocalDate disclosureStartDate;
    private LocalDate disclosureEndDate;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
