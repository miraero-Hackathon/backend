package org.jejuro.miraero.domain.aicoach.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;
import org.jejuro.miraero.domain.aicoach.dto.request.AiCoachMessageCreateRequest;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachMessageResponse;
import org.jejuro.miraero.domain.aicoach.service.AiCoachMessageService;
import org.jejuro.miraero.domain.aicoach.service.AiCoachStreamingService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

@ExtendWith(MockitoExtension.class)
class AiCoachMessageControllerTest {

    private static final Long USER_ID = 42L;
    private static final Long CONVERSATION_ID = 10L;

    @Mock
    private AiCoachMessageService aiCoachMessageService;

    @Mock
    private AiCoachStreamingService aiCoachStreamingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AiCoachMessageController(
                        aiCoachMessageService,
                        aiCoachStreamingService
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilters(new AuthenticationRequiredFilter())
                .build();
        authenticate();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("메시지 조회는 200과 메시지 목록을 반환한다")
    void getMessages_returnsOk() throws Exception {
        given(aiCoachMessageService.getMessages(USER_ID, CONVERSATION_ID)).willReturn(List.of(
                new AiCoachMessageResponse(
                        100L,
                        AiCoachMessageSenderType.USER,
                        "첫 번째 메시지",
                        LocalDateTime.of(2026, 8, 7, 10, 0)
                ),
                new AiCoachMessageResponse(
                        101L,
                        AiCoachMessageSenderType.ASSISTANT,
                        "두 번째 메시지",
                        LocalDateTime.of(2026, 8, 7, 10, 1)
                )
        ));

        mockMvc.perform(get("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].aiCoachMessageId").value(100L))
                .andExpect(jsonPath("$.data[0].senderType").value("USER"))
                .andExpect(jsonPath("$.data[0].content").value("첫 번째 메시지"))
                .andExpect(jsonPath("$.data[1].aiCoachMessageId").value(101L))
                .andExpect(jsonPath("$.data[1].senderType").value("ASSISTANT"));

        verify(aiCoachMessageService).getMessages(USER_ID, CONVERSATION_ID);
    }

    @Test
    @DisplayName("메시지가 없으면 data에 빈 배열을 담아 200을 반환한다")
    void getMessages_returnsEmptyArrayWhenMessagesDoNotExist() throws Exception {
        given(aiCoachMessageService.getMessages(USER_ID, CONVERSATION_ID)).willReturn(List.of());

        mockMvc.perform(get("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(aiCoachMessageService).getMessages(USER_ID, CONVERSATION_ID);
    }

    @Test
    @DisplayName("인증되지 않은 요청은 401을 반환한다")
    void getMessages_returnsUnauthorizedWithoutAuthentication() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(aiCoachMessageService);
    }

    @Test
    @DisplayName("대화방이 없거나 다른 사용자 소유이면 404를 반환한다")
    void getMessages_returnsNotFoundWhenConversationIsNotAccessible() throws Exception {
        willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND))
                .given(aiCoachMessageService)
                .getMessages(USER_ID, CONVERSATION_ID);

        mockMvc.perform(get("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));

        verify(aiCoachMessageService).getMessages(USER_ID, CONVERSATION_ID);
    }

    @Test
    @DisplayName("질문 전송 성공 시 201과 ASSISTANT 메시지 응답을 반환한다")
    void sendQuestion_returnsCreatedWithAssistantMessage() throws Exception {
        ArgumentCaptor<AiCoachMessageCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(AiCoachMessageCreateRequest.class);
        given(aiCoachMessageService.sendQuestion(
                eq(USER_ID),
                eq(CONVERSATION_ID),
                any(AiCoachMessageCreateRequest.class)
        )).willReturn(new AiCoachMessageResponse(
                100L,
                AiCoachMessageSenderType.ASSISTANT,
                "AI가 생성한 답변",
                LocalDateTime.of(2026, 8, 7, 10, 0)
        ));

        mockMvc.perform(post("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"자산 관리 방법을 알려주세요\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aiCoachMessageId").value(100L))
                .andExpect(jsonPath("$.data.senderType").value("ASSISTANT"))
                .andExpect(jsonPath("$.data.content").value("AI가 생성한 답변"));

        verify(aiCoachMessageService).sendQuestion(
                eq(USER_ID),
                eq(CONVERSATION_ID),
                requestCaptor.capture()
        );
        org.junit.jupiter.api.Assertions.assertEquals(
                "자산 관리 방법을 알려주세요",
                requestCaptor.getValue().getContent()
        );
    }

    @Test
    @DisplayName("content가 null이면 400을 반환한다")
    void sendQuestion_returnsBadRequestWhenContentIsNull() throws Exception {
        mockMvc.perform(post("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));

        verifyNoInteractions(aiCoachMessageService);
    }

    @Test
    @DisplayName("content가 빈 문자열이면 400을 반환한다")
    void sendQuestion_returnsBadRequestWhenContentIsEmpty() throws Exception {
        mockMvc.perform(post("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));

        verifyNoInteractions(aiCoachMessageService);
    }

    @Test
    @DisplayName("content가 공백 문자열이면 400을 반환한다")
    void sendQuestion_returnsBadRequestWhenContentIsBlank() throws Exception {
        mockMvc.perform(post("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));

        verifyNoInteractions(aiCoachMessageService);
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 질문 전송 요청은 401을 반환한다")
    void sendQuestion_returnsUnauthorizedWithoutAuthentication() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"메시지\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(aiCoachMessageService);
    }

    @Test
    @DisplayName("존재하지 않거나 다른 사용자 소유 대화방의 질문 전송 요청은 404를 반환한다")
    void sendQuestion_returnsNotFoundWhenConversationIsNotAccessible() throws Exception {
        willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND))
                .given(aiCoachMessageService)
                .sendQuestion(
                        eq(USER_ID),
                        eq(CONVERSATION_ID),
                        any(AiCoachMessageCreateRequest.class)
                );

        mockMvc.perform(post("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"메시지\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));

        verify(aiCoachMessageService).sendQuestion(
                eq(USER_ID),
                eq(CONVERSATION_ID),
                any(AiCoachMessageCreateRequest.class)
        );
    }

    @Test
    @DisplayName("OpenAI 호출에 실패하면 503을 반환한다")
    void sendQuestion_returnsServiceUnavailableWhenOpenAiFails() throws Exception {
        willThrow(new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE))
                .given(aiCoachMessageService)
                .sendQuestion(
                        eq(USER_ID),
                        eq(CONVERSATION_ID),
                        any(AiCoachMessageCreateRequest.class)
                );

        mockMvc.perform(post("/api/ai-coach/conversations/{conversationId}/messages", CONVERSATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"question\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_007"));

        verify(aiCoachMessageService).sendQuestion(
                eq(USER_ID),
                eq(CONVERSATION_ID),
                any(AiCoachMessageCreateRequest.class)
        );
    }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new AuthenticatedUser(USER_ID))
        );
    }

    private static class AuthenticationRequiredFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(request, response);
        }
    }
}
