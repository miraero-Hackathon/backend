package org.jejuro.miraero.domain.youthpolicy.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyListResponse;
import org.jejuro.miraero.domain.youthpolicy.service.YouthPolicyService;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.response.PageResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class YouthPolicyControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private YouthPolicyService youthPolicyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new YouthPolicyController(youthPolicyService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new AuthenticatedUser(USER_ID))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getYouthPolicies_usesAuthenticatedUserAndReturnsDDay() throws Exception {
        given(youthPolicyService.getYouthPolicies(USER_ID, null, null, null, 1, 10))
                .willReturn(PageResponse.of(List.of(new YouthPolicyListResponse(
                        1L,
                        "Policy",
                        "Keyword",
                        "Institution",
                        "2026-08-01 ~ 2026-08-31",
                        20L
                )), 0, 10, 1L));

        mockMvc.perform(get("/api/youth-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].dDay").value(20));

        verify(youthPolicyService).getYouthPolicies(USER_ID, null, null, null, 1, 10);
    }

    @Test
    void getRecommendedYouthPolicies_passesRegionFilter() throws Exception {
        given(youthPolicyService.getRecommendedYouthPolicies(USER_ID, "서울특별시"))
                .willReturn(PageResponse.of(List.of(), 0, 3, 0));

        mockMvc.perform(get("/api/youth-policies/recommended").param("region", "서울특별시"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(youthPolicyService).getRecommendedYouthPolicies(USER_ID, "서울특별시");
    }
}
