package org.jejuro.miraero.domain.transaction.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.transaction.domain.ExpenseCategoryTargetQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetItemRequest;

public interface ExpenseCategoryTargetMapper {

    List<ExpenseCategoryTargetQueryResult> findTargetsByUserId(@Param("userId") Long userId);

    List<ExpenseCategoryTargetQueryResult> findTargetsByUserIdAndCategoryIds(
            @Param("userId") Long userId,
            @Param("categoryIds") List<Long> categoryIds
    );

    long countExistingCategoriesByIds(@Param("categoryIds") List<Long> categoryIds);

    int upsertTargets(
            @Param("userId") Long userId,
            @Param("targets") List<ExpenseCategoryTargetItemRequest> targets
    );
}
