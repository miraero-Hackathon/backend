package org.jejuro.miraero.domain.aicoach.service;

import org.jejuro.miraero.domain.aicoach.context.AiCoachFinancialContext;

public interface AiCoachFinancialContextService {

    AiCoachFinancialContext getFinancialContext(Long userId);
}
