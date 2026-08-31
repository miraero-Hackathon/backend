package org.jejuro.miraero.domain.account.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.exception.AccountErrorCode;
import org.jejuro.miraero.domain.account.service.AccountService;
import org.jejuro.miraero.global.exception.BusinessException;
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
class AccountControllerTest {

  private static final Long USER_ID = 1L;

  @Mock
  private AccountService accountService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    AccountController accountController = new AccountController(accountService);

    mockMvc = MockMvcBuilders
        .standaloneSetup(accountController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();

    SecurityContextHolder.getContext().setAuthentication(
        new JwtAuthenticationToken(new AuthenticatedUser(USER_ID))
    );
  }

  @Test
  @DisplayName("로그인한 사용자의 계좌 목록과 잔액 합계를 반환한다")
  void getAccounts_success() throws Exception {
    AccountListResponse response = AccountListResponse.builder()
        .totalBalance(3_400_000L)
        .accounts(List.of(
            AccountResponse.builder()
                .accountId(1L)
                .accountType("CHECKING")
                .accountName("KB 입출금통장")
                .institutionName("국민은행")
                .maskedAccountNumber("123*****90")
                .balance(3_400_000L)
                .build()
        ))
        .build();
    given(accountService.getAccounts(eq(USER_ID), isNull(), eq(false))).willReturn(response);

    mockMvc.perform(get("/api/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalBalance").value(3_400_000))
        .andExpect(jsonPath("$.data.accounts[0].accountId").value(1))
        .andExpect(jsonPath("$.data.accounts[0].accountType").value("CHECKING"))
        .andExpect(jsonPath("$.data.accounts[0].institutionName").value("국민은행"));
  }

  @Test
  @DisplayName("accountType 쿼리파라미터를 서비스에 그대로 전달한다")
  void getAccounts_withAccountTypeFilter() throws Exception {
    AccountListResponse response = AccountListResponse.builder()
        .totalBalance(0L)
        .accounts(List.of())
        .build();
    given(accountService.getAccounts(USER_ID, "SAVINGS", false)).willReturn(response);

    mockMvc.perform(get("/api/accounts").param("accountType", "SAVINGS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("계좌 ID로 단건 상세를 조회한다")
  void getAccount_success() throws Exception {
    AccountResponse response = AccountResponse.builder()
        .accountId(1L)
        .accountType("CHECKING")
        .accountName("KB 입출금통장")
        .institutionName("국민은행")
        .maskedAccountNumber("123*****90")
        .balance(3_400_000L)
        .build();
    given(accountService.getAccount(1L, USER_ID)).willReturn(response);

    mockMvc.perform(get("/api/accounts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accountId").value(1))
        .andExpect(jsonPath("$.data.institutionName").value("국민은행"));
  }

  @Test
  @DisplayName("허용되지 않은 accountType이면 400을 반환한다")
  void getAccounts_invalidAccountType_returns400() throws Exception {
    given(accountService.getAccounts(USER_ID, "INVALID", false))
        .willThrow(new BusinessException(AccountErrorCode.INVALID_ACCOUNT_TYPE));

    mockMvc.perform(get("/api/accounts").param("accountType", "INVALID"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("accountId가 숫자 형식이 아니면 400을 반환한다")
  void getAccount_invalidIdFormat_returns400() throws Exception {
    mockMvc.perform(get("/api/accounts/abc"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }
}
