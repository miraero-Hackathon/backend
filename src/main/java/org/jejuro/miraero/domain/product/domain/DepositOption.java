package org.jejuro.miraero.domain.product.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositOption {

    private Long depositOptionId;
    private Long depositProductId;
    private String interestRateType;
    private Integer saveTerm;
    private BigDecimal baseInterestRate;
    private BigDecimal maxInterestRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
