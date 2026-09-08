package org.jejuro.miraero.domain.credit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.credit.domain.CreditScore;

@Mapper
public interface CreditScoreMapper {
    // 유저의 credit_score 행이 없으면 initialScore(665)로 만들고, 이미 있으면 아무 것도 안 함.
    void insertIfAbsent(@Param("userId") Long userId, @Param("initialScore") int initialScore);

    CreditScore findByUserId(@Param("userId") Long userId);

    void applyDelta(@Param("userId") Long userId, @Param("delta") int delta, @Param("minScore") int minScore, @Param("maxScore") int maxScore);
}
