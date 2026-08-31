package org.jejuro.miraero.domain.aicoach.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executor;
import org.jejuro.miraero.domain.aicoach.client.OpenAiClient;
import org.jejuro.miraero.domain.aicoach.client.OpenAiModerationClient;
import org.jejuro.miraero.domain.aicoach.context.AiCoachFinancialContext;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;
import org.jejuro.miraero.domain.aicoach.dto.request.AiCoachMessageCreateRequest;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachMessageResponse;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachConversationMapper;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachMessageMapper;
import org.jejuro.miraero.domain.aicoach.prompt.AiCoachPromptBuilder;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AiCoachStreamingService {

    private static final Logger log = LoggerFactory.getLogger(AiCoachStreamingService.class);
    private static final long EMITTER_TIMEOUT_MS = 90_000L;
    private static final int ANSWER_MAX_OUTPUT_TOKENS = 500;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int FALLBACK_TITLE_LENGTH = 50;

    private final AiCoachConversationMapper aiCoachConversationMapper;
    private final AiCoachMessageMapper aiCoachMessageMapper;
    private final AiCoachFinancialContextService aiCoachFinancialContextService;
    private final AiCoachPromptBuilder aiCoachPromptBuilder;
    private final OpenAiClient openAiClient;
    private final OpenAiModerationClient openAiModerationClient;
    private final AiCoachGuardrailService aiCoachGuardrailService;
    private final AiCoachSummaryAsyncService aiCoachSummaryAsyncService;
    private final PlatformTransactionManager transactionManager;
    private final Executor aiCoachStreamExecutor;

    public AiCoachStreamingService(
            AiCoachConversationMapper aiCoachConversationMapper,
            AiCoachMessageMapper aiCoachMessageMapper,
            AiCoachFinancialContextService aiCoachFinancialContextService,
            AiCoachPromptBuilder aiCoachPromptBuilder,
            OpenAiClient openAiClient,
            OpenAiModerationClient openAiModerationClient,
            AiCoachGuardrailService aiCoachGuardrailService,
            AiCoachSummaryAsyncService aiCoachSummaryAsyncService,
            PlatformTransactionManager transactionManager,
            @Qualifier("aiCoachStreamExecutor") Executor aiCoachStreamExecutor
    ) {
        this.aiCoachConversationMapper = aiCoachConversationMapper;
        this.aiCoachMessageMapper = aiCoachMessageMapper;
        this.aiCoachFinancialContextService = aiCoachFinancialContextService;
        this.aiCoachPromptBuilder = aiCoachPromptBuilder;
        this.openAiClient = openAiClient;
        this.openAiModerationClient = openAiModerationClient;
        this.aiCoachGuardrailService = aiCoachGuardrailService;
        this.aiCoachSummaryAsyncService = aiCoachSummaryAsyncService;
        this.transactionManager = transactionManager;
        this.aiCoachStreamExecutor = aiCoachStreamExecutor;
    }

    public SseEmitter streamQuestion(
            Long userId,
            Long conversationId,
            AiCoachMessageCreateRequest request
    ) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        try {
            AiCoachConversation conversation = getOwnedConversation(userId, conversationId);
            boolean firstQuestion = aiCoachMessageMapper.countByConversationId(conversationId) == 0;
            AiCoachGuardrailDecision inputDecision = inspectInput(request.getContent());
            if (!inputDecision.isAllowed()) {
                AiCoachMessageResponse response = executeInNewTransaction(() -> saveAssistantMessage(
                        userId,
                        conversationId,
                        inputDecision.getSafeMessage(),
                        firstQuestion ? getTitle(request.getContent()) : null
                ));
                sendComplete(emitter, response);
                return emitter;
            }

            executeInNewTransaction(() -> saveUserMessage(userId, conversationId, request));
            AiCoachFinancialContext context = aiCoachFinancialContextService.getFinancialContext(userId);
            String prompt = aiCoachPromptBuilder.buildStreamingPrompt(
                    context,
                    conversation.getConversationSummary(),
                    request.getContent()
            );
            sendEvent(emitter, "started", Map.of("conversationId", conversationId));
            aiCoachStreamExecutor.execute(() -> generateAnswer(
                    emitter,
                    userId,
                    conversationId,
                    request.getContent(),
                    firstQuestion,
                    prompt
            ));
        } catch (Exception exception) {
            sendError(emitter);
        }
        return emitter;
    }

    private void generateAnswer(
            SseEmitter emitter,
            Long userId,
            Long conversationId,
            String question,
            boolean firstQuestion,
            String prompt
    ) {
        long startedAt = System.nanoTime();
        StringBuilder answer = new StringBuilder();
        AtomicBoolean firstDeltaReceived = new AtomicBoolean();
        try {
            String generatedText = openAiClient.generateTextStream(
                    prompt,
                    ANSWER_MAX_OUTPUT_TOKENS,
                    delta -> sendDelta(
                            emitter,
                            answer,
                            delta,
                            startedAt,
                            firstDeltaReceived
                    )
            );
            if (!StringUtils.hasText(generatedText)) {
                throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
            }
            AiCoachGuardrailDecision outputDecision = inspectCompletedOutput(generatedText);
            if (!outputDecision.isAllowed()) {
                throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
            }
            AiCoachMessageResponse response = executeInNewTransaction(() -> saveAssistantMessage(
                    userId,
                    conversationId,
                    generatedText.trim(),
                    firstQuestion ? getTitle(question) : null
            ));
            aiCoachSummaryAsyncService.updateSummary(
                    userId,
                    conversationId,
                    question,
                    generatedText.trim()
            );
            sendComplete(emitter, response);
            log.info("AI 코치 스트림 완료. conversationId={}, elapsedMs={}",
                    conversationId, elapsedMillis(startedAt));
        } catch (Exception exception) {
            log.warn("AI 코치 스트림 실패. conversationId={}, elapsedMs={}",
                    conversationId, elapsedMillis(startedAt), exception);
            sendError(emitter);
        }
    }

    private void sendDelta(
            SseEmitter emitter,
            StringBuilder answer,
            String delta,
            long startedAt,
            AtomicBoolean firstDeltaReceived
    ) {
        AiCoachGuardrailDecision decision = aiCoachGuardrailService.inspectOutput(answer + delta);
        if (!decision.isAllowed()) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        answer.append(delta);
        sendEvent(emitter, "delta", Map.of("content", delta));
        if (firstDeltaReceived.compareAndSet(false, true)) {
            log.info("AI 코치 스트림 첫 delta. elapsedMs={}", elapsedMillis(startedAt));
        }
    }

    private AiCoachGuardrailDecision inspectInput(String content) {
        AiCoachGuardrailDecision ruleDecision = aiCoachGuardrailService.inspectInput(content);
        if (!ruleDecision.isAllowed()) {
            return ruleDecision;
        }
        return aiCoachGuardrailService.inspectModerationResult(openAiModerationClient.isFlagged(content));
    }

    private AiCoachGuardrailDecision inspectCompletedOutput(String content) {
        AiCoachGuardrailDecision ruleDecision = aiCoachGuardrailService.inspectOutput(content);
        if (!ruleDecision.isAllowed()) {
            return ruleDecision;
        }
        return aiCoachGuardrailService.inspectModerationResult(openAiModerationClient.isFlagged(content));
    }

    private AiCoachMessageResponse saveUserMessage(
            Long userId,
            Long conversationId,
            AiCoachMessageCreateRequest request
    ) {
        getOwnedConversation(userId, conversationId);
        LocalDateTime now = LocalDateTime.now();
        AiCoachMessage message = AiCoachMessage.builder()
                .aiCoachConversationId(conversationId)
                .senderType(AiCoachMessageSenderType.USER)
                .content(request.getContent())
                .createdAt(now)
                .build();
        if (aiCoachMessageMapper.save(message) != 1
                || aiCoachConversationMapper.updateLastMessageAt(userId, conversationId, now) != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return AiCoachMessageResponse.from(message);
    }

    private AiCoachMessageResponse saveAssistantMessage(
            Long userId,
            Long conversationId,
            String content,
            String title
    ) {
        LocalDateTime now = LocalDateTime.now();
        AiCoachMessage message = AiCoachMessage.builder()
                .aiCoachConversationId(conversationId)
                .senderType(AiCoachMessageSenderType.ASSISTANT)
                .content(content)
                .createdAt(now)
                .build();
        if (aiCoachMessageMapper.save(message) != 1
                || aiCoachConversationMapper.updateLastMessageAt(userId, conversationId, now) != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (title != null) {
            aiCoachConversationMapper.updateTitle(userId, conversationId, title);
        }
        return AiCoachMessageResponse.from(message);
    }

    private void sendComplete(SseEmitter emitter, AiCoachMessageResponse response) {
        sendEvent(emitter, "completed", Map.of("message", response));
        emitter.complete();
    }

    private void sendError(SseEmitter emitter) {
        sendEvent(emitter, "error", Map.of("message", "AI 응답을 생성하지 못했습니다."));
        emitter.complete();
    }

    private void sendEvent(SseEmitter emitter, String name, Map<String, ?> data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON));
        } catch (IOException exception) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private String getTitle(String question) {
        String title = question == null ? "AI 코치 상담" : question.trim();
        int maxLength = title.isBlank() ? MAX_TITLE_LENGTH : FALLBACK_TITLE_LENGTH;
        return title.length() <= maxLength ? title : title.substring(0, maxLength);
    }

    private <T> T executeInNewTransaction(TransactionCallback<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate.execute(status -> callback.execute());
    }

    private AiCoachConversation getOwnedConversation(Long userId, Long conversationId) {
        AiCoachConversation conversation = aiCoachConversationMapper.findByIdAndUserId(userId, conversationId);
        if (conversation == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
        return conversation;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    @FunctionalInterface
    private interface TransactionCallback<T> {

        T execute();
    }
}
