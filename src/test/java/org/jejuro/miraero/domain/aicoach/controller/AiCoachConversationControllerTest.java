package org.jejuro.miraero.domain.aicoach.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationCreateResponse;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationResponse;
import org.jejuro.miraero.domain.aicoach.service.AiCoachConversationService;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

@ExtendWith(MockitoExtension.class)
class AiCoachConversationControllerTest {

    private static final Long USER_ID = 42L;
    private static final Long CONVERSATION_ID = 10L;

    @Mock
    private AiCoachConversationService aiCoachConversationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AiCoachConversationController(aiCoachConversationService))
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
    @DisplayName("최근 대화방 조회는 200과 대화방 응답을 반환한다")
    void getLatestConversation_returnsOk() throws Exception {
        AiCoachConversationResponse response = new AiCoachConversationResponse(
                CONVERSATION_ID,
                "자산 관리 상담",
                LocalDateTime.of(2026, 8, 5, 9, 0),
                LocalDateTime.of(2026, 8, 5, 8, 0)
        );
        given(aiCoachConversationService.getLatestConversation(USER_ID)).willReturn(response);

        mockMvc.perform(get("/api/ai-coach/conversations/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aiCoachConversationId").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.data.title").value("자산 관리 상담"));

        verify(aiCoachConversationService).getLatestConversation(USER_ID);
    }

    @Test
    @DisplayName("최근 대화방이 없으면 data가 null인 200 응답을 반환한다")
    void getLatestConversation_returnsNullDataWhenConversationDoesNotExist() throws Exception {
        given(aiCoachConversationService.getLatestConversation(USER_ID)).willReturn(null);

        mockMvc.perform(get("/api/ai-coach/conversations/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(aiCoachConversationService).getLatestConversation(USER_ID);
    }

    @Test
    @DisplayName("대화방 생성은 201과 생성 응답을 반환한다")
    void createConversation_returnsCreated() throws Exception {
        AiCoachConversationCreateResponse response = new AiCoachConversationCreateResponse(
                CONVERSATION_ID,
                "새 대화",
                LocalDateTime.of(2026, 8, 5, 10, 0)
        );
        given(aiCoachConversationService.createConversation(USER_ID)).willReturn(response);

        mockMvc.perform(post("/api/ai-coach/conversations"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aiCoachConversationId").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.data.title").value("새 대화"));

        verify(aiCoachConversationService).createConversation(USER_ID);
    }

    @Test
    @DisplayName("대화방 목록 조회는 200과 목록 응답을 반환한다")
    void getConversations_returnsOk() throws Exception {
        given(aiCoachConversationService.getConversations(USER_ID)).willReturn(List.of(
                new AiCoachConversationResponse(
                        CONVERSATION_ID,
                        "첫 번째 대화",
                        LocalDateTime.of(2026, 8, 5, 9, 0),
                        LocalDateTime.of(2026, 8, 5, 8, 0)
                )
        ));

        mockMvc.perform(get("/api/ai-coach/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].aiCoachConversationId").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.data[0].title").value("첫 번째 대화"));

        verify(aiCoachConversationService).getConversations(USER_ID);
    }

    @Test
    @DisplayName("대화방 삭제는 200과 성공 응답을 반환한다")
    void deleteConversation_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/ai-coach/conversations/{conversationId}", CONVERSATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(aiCoachConversationService).deleteConversation(USER_ID, CONVERSATION_ID);
    }

    @Test
    @DisplayName("인증되지 않은 요청은 401을 반환한다")
    void getConversations_returnsUnauthorizedWithoutAuthentication() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/ai-coach/conversations"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(aiCoachConversationService);
    }

    @Test
    @DisplayName("삭제할 대화방이 없으면 404를 반환한다")
    void deleteConversation_returnsNotFoundWhenConversationDoesNotExist() throws Exception {
        willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND))
                .given(aiCoachConversationService)
                .deleteConversation(USER_ID, CONVERSATION_ID);

        mockMvc.perform(delete("/api/ai-coach/conversations/{conversationId}", CONVERSATION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));

        verify(aiCoachConversationService).deleteConversation(USER_ID, CONVERSATION_ID);
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
