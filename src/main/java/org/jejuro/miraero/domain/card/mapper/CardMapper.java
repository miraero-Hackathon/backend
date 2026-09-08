package org.jejuro.miraero.domain.card.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CardMapper {
    // 특정 유저가 보유한 체크카드(card_type='CHECK')의 card_id 목록
    List<Long> findCheckCardIdsByUserId(@Param("userId") Long userId);
}
