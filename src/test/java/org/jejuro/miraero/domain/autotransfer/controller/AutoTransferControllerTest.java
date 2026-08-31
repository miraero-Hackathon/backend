package org.jejuro.miraero.domain.autotransfer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.jejuro.miraero.domain.autotransfer.service.AutoTransferExecutionService;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
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

@ExtendWith(MockitoExtension.class)
class AutoTransferControllerTest {

  private static final Long USER_ID = 1L;

  @Mock
  private AutoTransferExecutionService autoTransferExecutionService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new AutoTransferController(autoTransferExecutionService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();

    SecurityContextHolder.getContext().setAuthentication(
        new JwtAuthenticationToken(new AuthenticatedUser(USER_ID))
    );
  }

  @Test
  @DisplayName("날짜를 주지 않으면 오늘 기준으로 실행한다")
  void execute_withoutDate_usesToday() throws Exception {
    given(autoTransferExecutionService.executeAll(eq(LocalDate.now()), eq(USER_ID)))
        .willReturn(1);

    mockMvc.perform(post("/api/auto-transfers/execute"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.executedCount").value(1));
  }

  @Test
  @DisplayName("과거 날짜를 주면 그날 기준으로 실행한다")
  void execute_withPastDate_usesGivenDate() throws Exception {
    LocalDate past = LocalDate.now().minusMonths(2);
    given(autoTransferExecutionService.executeAll(eq(past), eq(USER_ID)))
        .willReturn(1);

    mockMvc.perform(post("/api/auto-transfers/execute").param("date", past.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.executedCount").value(1));

    // 요청한 날짜가 그대로 서비스로 전달되는지 확인한다
    verify(autoTransferExecutionService).executeAll(past, USER_ID);
  }

  @Test
  @DisplayName("미래 날짜는 거부한다")
  void execute_futureDate_returns400() throws Exception {
    LocalDate future = LocalDate.now().plusDays(1);

    mockMvc.perform(post("/api/auto-transfers/execute").param("date", future.toString()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    verify(autoTransferExecutionService, never()).executeAll(any(), any());
  }

  @Test
  @DisplayName("이미 실행된 날짜면 적립 건수가 0으로 나온다")
  void execute_alreadyExecuted_returnsZero() throws Exception {
    given(autoTransferExecutionService.executeAll(any(), eq(USER_ID)))
        .willReturn(0);

    mockMvc.perform(post("/api/auto-transfers/execute"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.executedCount").value(0));
  }
}
