package org.jejuro.miraero.domain.checkcard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.checkcard.domain.CheckCardAwardState;

@Mapper
public interface CheckCardAwardStateMapper {
    CheckCardAwardState findByUserId(@Param("userId") Long userId);

    void upsert(CheckCardAwardState state);
}
