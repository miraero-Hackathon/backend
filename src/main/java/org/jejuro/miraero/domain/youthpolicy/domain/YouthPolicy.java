package org.jejuro.miraero.domain.youthpolicy.domain;

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
public class YouthPolicy {

    private String policyNo;
    private String policyName;
    private String policyKeyword;
    private String policyDescription;
    private String supportContent;
    private String providerInstitutionCode;
    private String providerInstitutionName;
    private String zipCd;
    private LocalDate applicationStartDate;
    private LocalDate applicationEndDate;
    private String applicationPeriodText;
    private String applicationMethod;
    private String applicationUrl;
    private String referenceUrl;
    private Integer minAge;
    private Integer maxAge;
    private String incomeConditionCode;
    private Long minIncome;
    private Long maxIncome;
    private String incomeConditionText;
    private String qualification;
    private LocalDateTime syncedAt;
}
