package org.jejuro.miraero.domain.transaction.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;
import java.util.Collections;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryMonthChangeResponse;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryThreeMonthAverageResponse;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageCategoryResponse;
import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseAnalysisService;
import org.jejuro.miraero.domain.transaction.service.PeerAverageService;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

@ExtendWith(MockitoExtension.class)
class ExpenseAnalysisControllerTest {
    private static final Long USER_ID = 42L;
    @Mock private ExpenseAnalysisService service;
    @Mock private PeerAverageService peerAverageService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExpenseAnalysisController(service, peerAverageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(new AuthenticatedUser(USER_ID)));
    }

    @Test
    void getDashboard_successAndEmptyData() throws Exception {
        YearMonth currentMonth = YearMonth.now();
        given(service.getDashboard(USER_ID, currentMonth.getYear(), currentMonth.getMonthValue())).willReturn(new ExpenseDashboardResponse(
                currentMonth.getYear(),
                currentMonth.getMonthValue(),
                new CategoryThreeMonthAverageResponse("2026-04", "2026-06", Collections.emptyList()),
                Collections.emptyList()
        ));
        mockMvc.perform(get("/api/expense-analysis/dashboard"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.recentTransactions").doesNotExist())
                .andExpect(jsonPath("$.data.categoryThreeMonthAverages.startMonth").value("2026-04"))
                .andExpect(jsonPath("$.data.peerCategoryAverages").doesNotExist());
        verify(service).getDashboard(USER_ID, currentMonth.getYear(), currentMonth.getMonthValue());
    }

    @Test
    void getDashboard_returnsCategoryMonthChanges() throws Exception {
        YearMonth currentMonth = YearMonth.now();
        given(service.getDashboard(USER_ID, currentMonth.getYear(), currentMonth.getMonthValue())).willReturn(new ExpenseDashboardResponse(
                currentMonth.getYear(),
                currentMonth.getMonthValue(),
                new CategoryThreeMonthAverageResponse("2026-04", "2026-06", Collections.emptyList()),
                Collections.singletonList(new CategoryMonthChangeResponse(1L, "food", 250000L, 280000L, 30000L))
        ));

        mockMvc.perform(get("/api/expense-analysis/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryMonthChanges[0].categoryId").value(1))
                .andExpect(jsonPath("$.data.categoryMonthChanges[0].previousMonthAmount").value(250000))
                .andExpect(jsonPath("$.data.categoryMonthChanges[0].currentMonthAmount").value(280000))
                .andExpect(jsonPath("$.data.categoryMonthChanges[0].changeAmount").value(30000));
    }

    @Test
    void getPeerAverage_returnsCurrentUserPeerAverage() throws Exception {
        given(peerAverageService.getPeerAverages(USER_ID)).willReturn(new PeerAverageResponse(Collections.singletonList(
                new PeerAverageCategoryResponse(1L, "식비", 285_000L)
        )));

        mockMvc.perform(get("/api/expense-analysis/peer-average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categories[0].categoryName").value("식비"))
                .andExpect(jsonPath("$.data.categories[0].peerAverageAmount").value(285000));

        verify(peerAverageService).getPeerAverages(USER_ID);
    }

}
