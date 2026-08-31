package org.jejuro.miraero.domain.aicoach.service;

import org.jejuro.miraero.domain.aicoach.client.OpenAiClient;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachConversationMapper;
import org.jejuro.miraero.domain.aicoach.prompt.AiCoachPromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiCoachSummaryAsyncService {

    private static final Logger log = LoggerFactory.getLogger(AiCoachSummaryAsyncService.class);
    private static final int SUMMARY_MAX_OUTPUT_TOKENS = 350;
    private static final int SUMMARY_MAX_LENGTH = 300;

    private final AiCoachConversationMapper aiCoachConversationMapper;
    private final AiCoachPromptBuilder aiCoachPromptBuilder;
    private final OpenAiClient openAiClient;

    public AiCoachSummaryAsyncService(
            AiCoachConversationMapper aiCoachConversationMapper,
            AiCoachPromptBuilder aiCoachPromptBuilder,
            OpenAiClient openAiClient
    ) {
        this.aiCoachConversationMapper = aiCoachConversationMapper;
        this.aiCoachPromptBuilder = aiCoachPromptBuilder;
        this.openAiClient = openAiClient;
    }

    @Async("aiCoachSummaryExecutor")
    public void updateSummary(
            Long userId,
            Long conversationId,
            String question,
            String answer
    ) {
        long startedAt = System.nanoTime();
        try {
            AiCoachConversation conversation = aiCoachConversationMapper.findByIdAndUserId(
                    userId,
                    conversationId
            );
            if (conversation == null) {
                return;
            }
            String prompt = aiCoachPromptBuilder.buildSummaryPrompt(
                    conversation.getConversationSummary(),
                    question,
                    answer
            );
            String summary = openAiClient.generateText(prompt, SUMMARY_MAX_OUTPUT_TOKENS);
            if (!StringUtils.hasText(summary)) {
                return;
            }
            aiCoachConversationMapper.updateConversationSummary(
                    userId,
                    conversationId,
                    truncate(summary.trim())
            );
            log.info("AI 코치 요약 생성 완료. conversationId={}, elapsedMs={}",
                    conversationId, elapsedMillis(startedAt));
        } catch (Exception exception) {
            log.warn("AI 코치 요약 생성 실패. conversationId={}, elapsedMs={}",
                    conversationId, elapsedMillis(startedAt));
        }
    }

    private String truncate(String value) {
        return value.length() <= SUMMARY_MAX_LENGTH
                ? value
                : value.substring(0, SUMMARY_MAX_LENGTH);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
