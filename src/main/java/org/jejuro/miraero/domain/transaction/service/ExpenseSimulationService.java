package org.jejuro.miraero.domain.transaction.service;

import org.jejuro.miraero.domain.transaction.dto.request.ExpenseSimulationRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseSimulationResponse;

public interface ExpenseSimulationService {
    ExpenseSimulationResponse simulate(Long userId, ExpenseSimulationRequest request);
}
