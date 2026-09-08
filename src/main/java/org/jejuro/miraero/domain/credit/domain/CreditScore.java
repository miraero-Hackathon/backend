package org.jejuro.miraero.domain.credit.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditScore {

    private Long creditScoreId;
    private Long userId;
    private int currentScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
