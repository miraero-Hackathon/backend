package org.jejuro.miraero.domain.aicoach.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.aicoach.context.AiCoachFinancialContext;

@Mapper
public interface AiCoachFinancialContextMapper {

    List<AiCoachFinancialContext.ActiveGoal> findActiveGoalsByUserId(@Param("userId") Long userId);

    Long sumAccountBalancesByUserId(@Param("userId") Long userId);

    Long sumMoneyBoxBalancesByUserId(@Param("userId") Long userId);

    Long sumLoanRemainingAmountsByUserId(@Param("userId") Long userId);
}
