package org.jejuro.miraero.domain.transaction.service;

import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetSaveRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryTargetListResponse;

public interface ExpenseCategoryTargetService {

    ExpenseCategoryTargetListResponse getTargets(Long userId);

    ExpenseCategoryTargetListResponse saveTargets(Long userId, ExpenseCategoryTargetSaveRequest request);
}
