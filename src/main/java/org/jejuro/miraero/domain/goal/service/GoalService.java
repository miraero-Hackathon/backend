package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.goal.domain.GoalStatus;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalUpdateRequest;
import org.jejuro.miraero.domain.goal.dto.response.*;

import java.util.List;

public interface GoalService {
    GoalPossibilityResponse checkPossibility(
        GoalPossibilityRequest request,
        Long userId
    );

    GoalCreateResponse createGoal(
        Long userId,
        GoalCreateRequest request
    );

    List<GoalListResponse> getGoalsByUserId(Long userId);

    GoalDetailResponse getGoalDetail(Long userId, Long goalId);

    void updateGoal(
            Long userId, Long goalId, GoalUpdateRequest request
            );

    void deleteGoal(Long userId, Long goalId);
    void saveCollection(Long userId, Long goalId);

    List<GoalCollectionResponse> getGoalCollections(Long userId);

    void updateGoalStatus(Long userId, Long goalId, GoalStatus status);
}
