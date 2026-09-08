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
public class CreditScoreEvent {
    private Long creditScoreEventId;
    private Long userId;
    private CreditScoreReasonCode reasonCode;
    private int delta;
    private int scoreAfter;
    private String description;
    private LocalDateTime occurredAt;
}
