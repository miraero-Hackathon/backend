package org.jejuro.miraero.domain.pacemaker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerGoalDepositRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDepositAssetResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalListResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWithdrawalAccountResponse;
import org.jejuro.miraero.domain.pacemaker.exception.PaceMakerErrorCode;
import org.jejuro.miraero.domain.pacemaker.service.PaceMakerService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.response.PageResponse;
import org.jejuro.miraero.domain.pacemaker.service.PaceMakerSavingService;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class PaceMakerControllerTest {

    private static final Long USER_ID = 42L;

    @Mock
    private PaceMakerService paceMakerService;

    @Mock
    private PaceMakerSavingService paceMakerSavingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaceMakerController paceMakerController = new PaceMakerController(paceMakerService, paceMakerSavingService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(paceMakerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new AuthenticatedUser(USER_ID))
        );
    }

    @Test
    @DisplayName("Get pace maker active status")
    void getPaceMaker_active() throws Exception {
        PaceMakerResponse response = PaceMakerResponse.builder()
                .autoSavingId(21L)
                .moneyBoxId(3L)
                .registered(true)
                .status("ACTIVE")
                .enabled(true)
                .build();
        given(paceMakerService.getPaceMaker(USER_ID)).willReturn(response);

        mockMvc.perform(get("/api/pace-maker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.autoSavingId").value(21L))
                .andExpect(jsonPath("$.data.moneyBoxId").value(3L))
                .andExpect(jsonPath("$.data.registered").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.enabled").value(true));

        verify(paceMakerService).getPaceMaker(USER_ID);
    }

    @Test
    @DisplayName("Get pace maker not created status")
    void getPaceMaker_notCreated() throws Exception {
        PaceMakerResponse response = PaceMakerResponse.builder()
                .autoSavingId(null)
                .registered(false)
                .status(null)
                .enabled(false)
                .build();
        given(paceMakerService.getPaceMaker(USER_ID)).willReturn(response);

        mockMvc.perform(get("/api/pace-maker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.autoSavingId").doesNotExist())
                .andExpect(jsonPath("$.data.registered").value(false))
                .andExpect(jsonPath("$.data.status").doesNotExist())
                .andExpect(jsonPath("$.data.enabled").value(false));

        verify(paceMakerService).getPaceMaker(USER_ID);
    }

    @Test
    @DisplayName("Update pace maker status")
    void updatePaceMaker_success() throws Exception {
        PaceMakerResponse response = PaceMakerResponse.builder()
                .autoSavingId(21L)
                .registered(true)
                .status("PAUSED")
                .enabled(false)
                .build();
        given(paceMakerService.updateStatus(USER_ID, 21L, "PAUSED")).willReturn(response);

        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/status", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAUSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.autoSavingId").value(21L))
                .andExpect(jsonPath("$.data.registered").value(true))
                .andExpect(jsonPath("$.data.status").value("PAUSED"))
                .andExpect(jsonPath("$.data.enabled").value(false));

        verify(paceMakerService).updateStatus(USER_ID, 21L, "PAUSED");
    }

    @Test
    @DisplayName("Invalid pace maker status returns 400")
    void updatePaceMaker_invalidStatus() throws Exception {
        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/status", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFF\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    @DisplayName("Update missing pace maker returns 404")
    void updatePaceMaker_notFound() throws Exception {
        given(paceMakerService.updateStatus(USER_ID, 99L, "ACTIVE"))
                .willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/status", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));

        verify(paceMakerService).updateStatus(USER_ID, 99L, "ACTIVE");
    }

    @Test
    @DisplayName("Update max amount")
    void updateMaxAmount_success() throws Exception {
        PaceMakerMaxAmountUpdateResponse response = PaceMakerMaxAmountUpdateResponse.builder()
                .autoSavingId(21L)
                .maxAmount(500_000L)
                .build();
        given(paceMakerService.updateMaxAmount(USER_ID, 21L, 500_000L)).willReturn(response);

        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/max-amount", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxAmount\":500000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.autoSavingId").value(21L))
                .andExpect(jsonPath("$.data.maxAmount").value(500_000L));

        verify(paceMakerService).updateMaxAmount(USER_ID, 21L, 500_000L);
    }

    @Test
    @DisplayName("Invalid max amount returns 400")
    void updateMaxAmount_invalidAmount() throws Exception {
        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/max-amount", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxAmount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    @DisplayName("Update max amount not found")
    void updateMaxAmount_notFound() throws Exception {
        given(paceMakerService.updateMaxAmount(USER_ID, 99L, 500_000L))
                .willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/max-amount", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxAmount\":500000}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));

        verify(paceMakerService).updateMaxAmount(USER_ID, 99L, 500_000L);
    }

    @Test
    @DisplayName("Get pace maker histories")
    void getHistories_success() throws Exception {
        PaceMakerHistoryResponse history = PaceMakerHistoryResponse.builder()
                .date("2026-08-05")
                .status("SAVED")
                .amount(10_000L)
                .description(null)
                .build();
        PageResponse<PaceMakerHistoryResponse> response =
                PageResponse.of(List.of(history), 0, 20, 1L);
        given(paceMakerService.getHistories(
                eq(USER_ID),
                any(PaceMakerHistorySearchCondition.class)
        )).willReturn(response);

        mockMvc.perform(get("/api/pace-maker/histories")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].date").value("2026-08-05"))
                .andExpect(jsonPath("$.data.content[0].status").value("SAVED"))
                .andExpect(jsonPath("$.data.content[0].amount").value(10_000L))
                .andExpect(jsonPath("$.data.content[0].description").doesNotExist())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1L))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));

        verify(paceMakerService).getHistories(
                eq(USER_ID),
                any(PaceMakerHistorySearchCondition.class)
        );
    }

    @Test
    @DisplayName("Invalid history query returns 400")
    void getHistories_invalidCondition() throws Exception {
        given(paceMakerService.getHistories(
                eq(USER_ID),
                any(PaceMakerHistorySearchCondition.class)
        )).willThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE));

        mockMvc.perform(get("/api/pace-maker/histories")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));

        verify(paceMakerService).getHistories(
                eq(USER_ID),
                any(PaceMakerHistorySearchCondition.class)
        );
    }

    @Test
    @DisplayName("Get pace maker goals")
    void getPaceMakerGoals_success() throws Exception {
        PaceMakerGoalListResponse response = PaceMakerGoalListResponse.builder()
                .goals(List.of(
                        PaceMakerGoalResponse.builder()
                                .goalId(1L)
                                .goalName("Travel")
                                .goalType("TRAVEL")
                                .goalAmount(3_000_000L)
                                .totalSavedAmount(1_200_000L)
                                .depositAssets(List.of(
                                        PaceMakerDepositAssetResponse.builder()
                                                .assetType("ACCOUNT")
                                                .assetId(3L)
                                                .financialInstitutionName("KB")
                                                .maskedAccountNumber("123-***-789")
                                                .balance(700_000L)
                                                .build()
                                ))
                                .withdrawalAccounts(List.of(
                                        PaceMakerWithdrawalAccountResponse.builder()
                                                .accountId(8L)
                                                .financialInstitutionName("KB")
                                                .maskedAccountNumber("987-***-123")
                                                .balance(1_000_000L)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        given(paceMakerService.getPaceMakerGoals(USER_ID)).willReturn(response);

        mockMvc.perform(get("/api/pace-maker/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.goals[0].goalId").value(1L))
                .andExpect(jsonPath("$.data.goals[0].goalName").value("Travel"))
                .andExpect(jsonPath("$.data.goals[0].goalType").value("TRAVEL"))
                .andExpect(jsonPath("$.data.goals[0].goalAmount").value(3_000_000L))
                .andExpect(jsonPath("$.data.goals[0].totalSavedAmount").value(1_200_000L))
                .andExpect(jsonPath("$.data.goals[0].depositAssets[0].assetType").value("ACCOUNT"))
                .andExpect(jsonPath("$.data.goals[0].depositAssets[0].financialInstitutionName").value("KB"))
                .andExpect(jsonPath("$.data.goals[0].depositAssets[0].balance").value(700_000L))
                .andExpect(jsonPath("$.data.goals[0].withdrawalAccounts[0].accountId").value(8L))
                .andExpect(jsonPath("$.data.goals[0].withdrawalAccounts[0].balance").value(1_000_000L));

        verify(paceMakerService).getPaceMakerGoals(USER_ID);
    }

    @Test
    @DisplayName("Get pace maker goals not registered")
    void getPaceMakerGoals_notRegistered() throws Exception {
        given(paceMakerService.getPaceMakerGoals(USER_ID))
                .willThrow(new BusinessException(PaceMakerErrorCode.NOT_REGISTERED));

        mockMvc.perform(get("/api/pace-maker/goals"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PACEMAKER_001"));

        verify(paceMakerService).getPaceMakerGoals(USER_ID);
    }

    @Test
    @DisplayName("Deposit pace maker balance to goal")
    void depositToGoal_success() throws Exception {
        PaceMakerGoalDepositResponse response = PaceMakerGoalDepositResponse.builder()
                .assetType(org.jejuro.miraero.domain.goal.domain.AssetType.ACCOUNT)
                .assetId(8L)
                .depositedAmount(270_000L)
                .remainingBalance(0L)
                .build();
        given(paceMakerService.depositToGoal(eq(USER_ID), any(PaceMakerGoalDepositRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/pace-maker/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moneyBoxId\":3,\"assetType\":\"ACCOUNT\",\"assetId\":8,\"amount\":270000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetType").value("ACCOUNT"))
                .andExpect(jsonPath("$.data.assetId").value(8L))
                .andExpect(jsonPath("$.data.depositedAmount").value(270_000L))
                .andExpect(jsonPath("$.data.remainingBalance").value(0L));

        verify(paceMakerService).depositToGoal(eq(USER_ID), any(PaceMakerGoalDepositRequest.class));
    }

    @Test
    @DisplayName("Deposit invalid amount returns 400")
    void depositToGoal_invalidAmount() throws Exception {
        mockMvc.perform(post("/api/pace-maker/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moneyBoxId\":3,\"assetType\":\"ACCOUNT\",\"assetId\":8,\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    @DisplayName("Deposit insufficient balance returns 400")
    void depositToGoal_insufficientBalance() throws Exception {
        given(paceMakerService.depositToGoal(eq(USER_ID), any(PaceMakerGoalDepositRequest.class)))
                .willThrow(new BusinessException(PaceMakerErrorCode.INSUFFICIENT_BALANCE));

        mockMvc.perform(post("/api/pace-maker/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moneyBoxId\":3,\"assetType\":\"ACCOUNT\",\"assetId\":8,\"amount\":270000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PACEMAKER_002"));

        verify(paceMakerService).depositToGoal(eq(USER_ID), any(PaceMakerGoalDepositRequest.class));
    }
}