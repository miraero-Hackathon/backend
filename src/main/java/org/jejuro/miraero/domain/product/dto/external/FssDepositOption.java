package org.jejuro.miraero.domain.product.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FssDepositOption {

    @JsonProperty("dcls_month")
    private String disclosureMonth;

    @JsonProperty("fin_co_no")
    private String financialCompanyCode;

    @JsonProperty("fin_prdt_cd")
    private String financialProductCode;

    @JsonProperty("intr_rate_type")
    private String interestRateType;

    @JsonProperty("intr_rate_type_nm")
    private String interestRateTypeName;

    @JsonProperty("save_trm")
    private String saveTerm;

    @JsonProperty("intr_rate")
    private BigDecimal interestRate;

    @JsonProperty("intr_rate2")
    private BigDecimal maximumInterestRate;
}
