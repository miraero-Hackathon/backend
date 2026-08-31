package org.jejuro.miraero.domain.aicoach.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiCoachMessageServiceImpl implements AiCoachMessageService {

    private final AiCoachConversationMapper aiCoachConversationMapper;
    private final AiCoachMessageMapper aiCoachMessageMapper;
    private final AiCoachFinancialContextService aiCoachFinancialContextService;
    private final AiCoachPromptBuilder aiCoachPromptBuilder;
    private final OpenAiClient openAiClient;
    private final OpenAiModerationClient openAiModerationClient;
    private final AiCoachGuardrailService aiCoachGuardrailService;
    private final PlatformTransactionManager transactionManager;

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int FALLBACK_TITLE_LENGTH = 50;
    private static final String DEFAULT_CONVERSATION_TITLE = "AI 코치 상담";
    private final AiCoachResponseParser aiCoachResponseParser = new AiCoachResponseParser();

    @Override
    @Transactional(readOnly = true)
    public List<AiCoachMessageResponse> getMessages(Long userId, Long conversationId) {
        getOwnedConversation(userId, conversationId);

        return aiCoachMessageMapper.findAllByConversationId(conversationId).stream()
                .map(AiCoachMessageResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public AiCoachMessageResponse saveUserMessage(
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

        int savedCount = aiCoachMessageMapper.save(message);
        if (savedCount != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        int updatedCount = aiCoachConversationMapper.updateLastMessageAt(userId, conversationId, now);
        if (updatedCount != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        return AiCoachMessageResponse.from(message);
    }

    @Override
    public AiCoachMessageResponse sendQuestion(
            Long userId,
            Long conversationId,
            AiCoachMessageCreateRequest request
    ) {
        AiCoachConversation conversation = getOwnedConversation(userId, conversationId);
        boolean firstQuestion = aiCoachMessageMapper.countByConversationId(conversationId) == 0;

        AiCoachGuardrailDecision inputDecision = inspectInput(request.getContent());
        if (!inputDecision.isAllowed()) {
            return executeInNewTransaction(() -> saveGuardrailMessage(
                    userId,
                    conversationId,
                    inputDecision.getSafeMessage()
            ));
        }

        executeInNewTransaction(() -> saveUserMessage(userId, conversationId, request));

        AiCoachFinancialContext financialContext = aiCoachFinancialContextService.getFinancialContext(userId);
        String prompt = aiCoachPromptBuilder.buildPrompt(
                financialContext,
                conversation.getConversationSummary(),
                request.getContent(),
                firstQuestion
        );
        String generatedText = openAiClient.generateText(prompt);
        AiCoachResponseParser.ParsedResponse parsedResponse = aiCoachResponseParser.parse(generatedText);
        String answer = parsedResponse.getAnswer();
        String conversationSummary = parsedResponse.getSummary();
        if (!StringUtils.hasText(answer) || !StringUtils.hasText(conversationSummary)) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        String title = firstQuestion
                ? getTitle(parsedResponse.getTitle(), request.getContent())
                : null;

        AiCoachGuardrailDecision outputDecision = inspectOutput(answer);
        if (!outputDecision.isAllowed()) {
            return executeInNewTransaction(() -> saveGuardrailMessage(
                    userId,
                    conversationId,
                    outputDecision.getSafeMessage()
            ));
        }

        return executeInNewTransaction(() -> saveAssistantMessage(
                userId,
                conversationId,
                answer,
                title,
                conversationSummary
        ));
    }

    private AiCoachGuardrailDecision inspectInput(String content) {
        AiCoachGuardrailDecision ruleDecision = aiCoachGuardrailService.inspectInput(content);
        if (!ruleDecision.isAllowed()) {
            return ruleDecision;
        }
        return aiCoachGuardrailService.inspectModerationResult(openAiModerationClient.isFlagged(content));
    }

    private AiCoachGuardrailDecision inspectOutput(String content) {
        AiCoachGuardrailDecision ruleDecision = aiCoachGuardrailService.inspectOutput(content);
        if (!ruleDecision.isAllowed()) {
            return ruleDecision;
        }
        return aiCoachGuardrailService.inspectModerationResult(openAiModerationClient.isFlagged(content));
    }

    private AiCoachMessageResponse saveAssistantMessage(
            Long userId,
            Long conversationId,
            String content,
            String title,
            String conversationSummary
    ) {
        LocalDateTime now = LocalDateTime.now();
        AiCoachMessage message = AiCoachMessage.builder()
                .aiCoachConversationId(conversationId)
                .senderType(AiCoachMessageSenderType.ASSISTANT)
                .content(content)
                .createdAt(now)
                .build();

        if (aiCoachMessageMapper.save(message) != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (title != null) {
            aiCoachConversationMapper.updateTitle(userId, conversationId, title);
        }
        if (aiCoachConversationMapper.updateConversationSummary(
                userId,
                conversationId,
                conversationSummary
        ) != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (aiCoachConversationMapper.updateLastMessageAt(userId, conversationId, now) != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return AiCoachMessageResponse.from(message);
    }

    private AiCoachMessageResponse saveGuardrailMessage(
            Long userId,
            Long conversationId,
            String content
    ) {
        LocalDateTime now = LocalDateTime.now();
        AiCoachMessage message = AiCoachMessage.builder()
                .aiCoachConversationId(conversationId)
                .senderType(AiCoachMessageSenderType.ASSISTANT)
                .content(content)
                .createdAt(now)
                .build();

        if (aiCoachMessageMapper.save(message) != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (aiCoachConversationMapper.updateLastMessageAt(userId, conversationId, now) != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return AiCoachMessageResponse.from(message);
    }

    private String getTitle(String parsedTitle, String userQuestion) {
        if (StringUtils.hasText(parsedTitle)) {
            return truncateTitle(parsedTitle);
        }
        if (StringUtils.hasText(userQuestion)) {
            return truncate(userQuestion.trim(), FALLBACK_TITLE_LENGTH);
        }
        return DEFAULT_CONVERSATION_TITLE;
    }

    private String truncateTitle(String title) {
        return truncate(title.trim(), MAX_TITLE_LENGTH);
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private <T> T executeInNewTransaction(TransactionCallback<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate.execute(status -> callback.execute());
    }

    @FunctionalInterface
    private interface TransactionCallback<T> {

        T execute();
    }

    private AiCoachConversation getOwnedConversation(Long userId, Long conversationId) {
        AiCoachConversation conversation = aiCoachConversationMapper.findByIdAndUserId(userId, conversationId);

        if (conversation == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
        return conversation;
    }
}
