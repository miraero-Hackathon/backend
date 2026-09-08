package org.jejuro.miraero.domain.utilitypayment.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityPaymentState;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityType;

@Mapper
public interface UtilityPaymentStateMapper {

    UtilityPaymentState findByUserIdAndType(
            @Param("userId") Long userId,
            @Param("utilityType") UtilityType utilityType
    );

    // 화면에 5개 항목 진행률을 한 번에 보여줘야 해서, 이 유저의 상태 전부를 한 번에 가져오는 조회도 필요.
    List<UtilityPaymentState> findByUserId(@Param("userId") Long userId);

    void upsert(UtilityPaymentState state);
}
