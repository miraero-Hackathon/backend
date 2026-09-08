package org.jejuro.miraero.domain.loansimulation.service;

import org.jejuro.miraero.domain.loansimulation.dto.response.LoanSimulationDetailResponse;

public interface LoanSimulationQueryService {
    LoanSimulationDetailResponse getDetail(Long userId, Long goalId);
}
