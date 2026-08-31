package org.jejuro.miraero.domain.transaction.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.domain.ExpenseCategoryTargetQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetItemRequest;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetSaveRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryTargetListResponse;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryTargetResponse;
import org.jejuro.miraero.domain.transaction.mapper.ExpenseCategoryTargetMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryTargetServiceImpl implements ExpenseCategoryTargetService {

    private final ExpenseCategoryTargetMapper expenseCategoryTargetMapper;

    @Override
    @Transactional(readOnly = true)
    public ExpenseCategoryTargetListResponse getTargets(Long userId) {
        validateUserId(userId);

        return toResponse(expenseCategoryTargetMapper.findTargetsByUserId(userId));
    }

    @Override
    @Transactional
    public ExpenseCategoryTargetListResponse saveTargets(
            Long userId,
            ExpenseCategoryTargetSaveRequest request
    ) {
        validateUserId(userId);
        List<Long> categoryIds = validateRequest(request);
        validateCategoriesExist(categoryIds);

        expenseCategoryTargetMapper.upsertTargets(userId, request.getTargets());

        return toResponse(
                expenseCategoryTargetMapper.findTargetsByUserIdAndCategoryIds(userId, categoryIds)
        );
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<Long> validateRequest(ExpenseCategoryTargetSaveRequest request) {
        if (request == null || request.getTargets() == null || request.getTargets().isEmpty()) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        Set<Long> categoryIds = new HashSet<>();
        for (ExpenseCategoryTargetItemRequest target : request.getTargets()) {
            if (target == null
                    || target.getCategoryId() == null
                    || target.getCategoryId() <= 0
                    || target.getTargetAmount() == null
                    || target.getTargetAmount() < 0
                    || !categoryIds.add(target.getCategoryId())) {
                throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
            }
        }

        return request.getTargets().stream()
                .map(ExpenseCategoryTargetItemRequest::getCategoryId)
                .collect(Collectors.toList());
    }

    private void validateCategoriesExist(List<Long> categoryIds) {
        if (expenseCategoryTargetMapper.countExistingCategoriesByIds(categoryIds)
                != categoryIds.size()) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    private ExpenseCategoryTargetListResponse toResponse(
            List<ExpenseCategoryTargetQueryResult> results
    ) {
        List<ExpenseCategoryTargetResponse> targets = results.stream()
                .map(ExpenseCategoryTargetResponse::from)
                .collect(Collectors.toList());

        return new ExpenseCategoryTargetListResponse(targets);
    }
}
