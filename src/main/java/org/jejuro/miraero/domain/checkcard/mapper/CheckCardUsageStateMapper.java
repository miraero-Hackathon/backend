package org.jejuro.miraero.domain.checkcard.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.checkcard.domain.CheckCardUsageState;

@Mapper
public interface CheckCardUsageStateMapper {
    CheckCardUsageState findByCardId(@Param("cardId") Long cardId);

    // 상태 화면(상위 2개 카드 정렬)에 필요한, 유저의 카드별 상태 전부
    List<CheckCardUsageState> findByUserId(@Param("userId") Long userId);

    void upsert(CheckCardUsageState state);
}
