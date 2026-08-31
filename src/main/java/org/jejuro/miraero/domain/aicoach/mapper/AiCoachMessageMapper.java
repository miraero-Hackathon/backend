package org.jejuro.miraero.domain.aicoach.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;

@Mapper
public interface AiCoachMessageMapper {

    List<AiCoachMessage> findAllByConversationId(@Param("conversationId") Long conversationId);

    int save(AiCoachMessage message);

    int countByConversationId(@Param("conversationId") Long conversationId);
}
