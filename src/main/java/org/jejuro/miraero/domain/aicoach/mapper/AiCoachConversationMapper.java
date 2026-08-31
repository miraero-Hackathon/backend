package org.jejuro.miraero.domain.aicoach.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;

@Mapper
public interface AiCoachConversationMapper {

    AiCoachConversation findLatestByUserId(@Param("userId") Long userId);

    List<AiCoachConversation> findAllByUserId(@Param("userId") Long userId);

    int save(AiCoachConversation conversation);

    AiCoachConversation findByIdAndUserId(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId
    );

    int deleteByIdAndUserId(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId
    );

    int updateTitle(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId,
            @Param("title") String title
    );

    int updateConversationSummary(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId,
            @Param("conversationSummary") String conversationSummary
    );

    int updateLastMessageAt(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId,
            @Param("lastMessageAt") LocalDateTime lastMessageAt
    );
}
