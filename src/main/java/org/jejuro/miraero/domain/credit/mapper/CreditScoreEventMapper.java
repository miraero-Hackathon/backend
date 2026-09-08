package org.jejuro.miraero.domain.credit.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.credit.domain.CreditScoreEvent;
import org.jejuro.miraero.domain.credit.dto.request.CreditScoreEventSearchCondition;

@Mapper
public interface CreditScoreEventMapper {

    void save(CreditScoreEvent creditScoreEvent);

    List<CreditScoreEvent> findEventByUserId(@Param("userId") Long userId, @Param("condition") CreditScoreEventSearchCondition condition);

    long countEventByUserId(@Param("userId") Long userId);

}
