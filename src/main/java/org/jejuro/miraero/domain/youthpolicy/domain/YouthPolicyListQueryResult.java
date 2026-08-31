package org.jejuro.miraero.domain.youthpolicy.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YouthPolicyListQueryResult {

    private Long youthPolicyId;
    private String policyName;
    private String policyKeyword;
    private String providerInstitutionName;
    private LocalDate applicationStartDate;
    private LocalDate applicationEndDate;
    private String applicationPeriodText;
}
