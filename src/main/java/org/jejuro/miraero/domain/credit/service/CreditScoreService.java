package org.jejuro.miraero.domain.credit.service;

import java.time.LocalDateTime;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.dto.request.CreditScoreEventSearchCondition;
import org.jejuro.miraero.domain.credit.dto.response.CreditScoreEventResponse;
import org.jejuro.miraero.domain.credit.dto.response.CreditScoreResponse;
import org.jejuro.miraero.global.response.PageResponse;
public interface CreditScoreService {

    CreditScoreResponse getCurrentScore(Long userId);

    PageResponse<CreditScoreEventResponse> getHistory(Long userId, CreditScoreEventSearchCondition condition);

    void applyEvent(
            Long userId,
            int delta,
            CreditScoreReasonCode reasonCode,
            String description,
            LocalDateTime occurredAt
    );

    void initializeCreditScore(Long userId);
}
