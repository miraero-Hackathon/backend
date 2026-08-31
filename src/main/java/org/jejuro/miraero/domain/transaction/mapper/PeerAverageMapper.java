package org.jejuro.miraero.domain.transaction.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.transaction.domain.PeerCategoryAverageQueryResult;

public interface PeerAverageMapper {

    List<PeerCategoryAverageQueryResult> findCategoryPeerAverages(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
