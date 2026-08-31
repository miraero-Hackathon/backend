package org.jejuro.miraero.domain.goal.dto.response.asset;


import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class AssetDetailResponse {
    private BigDecimal interestRate;
    private LocalDate maturityDate;
}
