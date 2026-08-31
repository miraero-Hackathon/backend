package org.jejuro.miraero.domain.pacemaker.service;

import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerGoalDepositRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerCreateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerCreateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalListResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.global.response.PageResponse;

public interface PaceMakerService {

  PaceMakerCreateResponse createPaceMaker(Long userId, PaceMakerCreateRequest request);

  PaceMakerResponse getPaceMaker(Long userId);

  PaceMakerResponse updateStatus(Long userId, Long autoSavingId, String status);

  PaceMakerDashboardResponse getDashboard(Long userId, boolean includeStreak);

  PaceMakerMaxAmountUpdateResponse updateMaxAmount(Long userId, Long autoSavingId, Long maxAmount);

  PageResponse<PaceMakerHistoryResponse> getHistories(
      Long userId,
      PaceMakerHistorySearchCondition condition
  );

  PaceMakerGoalListResponse getPaceMakerGoals(Long userId);

  PaceMakerGoalDepositResponse depositToGoal(
      Long userId,
      PaceMakerGoalDepositRequest request
  );
}