package org.jejuro.miraero.domain.transaction.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseSimulationRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseSimulationCategoryResponse;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseSimulationResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseSimulationService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class ExpenseSimulationControllerTest {

    private static final Long USER_ID = 42L;
    private static final String VALID_REQUEST = "{\"year\":2026,\"month\":7,\"categories\":[{\"categoryId\":1,\"targetExpense\":250000}]}";

    @Mock
    private ExpenseSimulationService expenseSimulationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExpenseSimulationController(expenseSimulationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
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
    void simulate_returnsResponseAndPassesAuthenticatedUserAndRequest() throws Exception {
        ExpenseSimulationResponse response = new ExpenseSimulationResponse(
                2026,
                7,
                320000L,
                250000L,
                70000L,
                List.of(new ExpenseSimulationCategoryResponse(1L, "식비", 320000L, 250000L, 70000L))
        );
        given(expenseSimulationService.simulate(eq(USER_ID), org.mockito.ArgumentMatchers.any(ExpenseSimulationRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/expense-analysis/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.currentTotalExpense").value(320000))
                .andExpect(jsonPath("$.data.categories[0].categoryName").value("식비"))
                .andExpect(jsonPath("$.data.categories[0].reductionAmount").value(70000));

        ArgumentCaptor<ExpenseSimulationRequest> requestCaptor =
                ArgumentCaptor.forClass(ExpenseSimulationRequest.class);
        verify(expenseSimulationService).simulate(eq(USER_ID), requestCaptor.capture());
        assertEquals(2026, requestCaptor.getValue().getYear());
        assertEquals(7, requestCaptor.getValue().getMonth());
        assertEquals(1L, requestCaptor.getValue().getCategories().get(0).getCategoryId());
        assertEquals(250000L, requestCaptor.getValue().getCategories().get(0).getTargetExpense());
    }

    @Test
    void simulate_rejectsInvalidRequestWithoutCallingService() throws Exception {
        assertInvalidRequest("{\"month\":7,\"categories\":[{\"categoryId\":1,\"targetExpense\":250000}]}");
        assertInvalidRequest("{\"year\":2026,\"month\":0,\"categories\":[{\"categoryId\":1,\"targetExpense\":250000}]}");
        assertInvalidRequest("{\"year\":2026,\"month\":13,\"categories\":[{\"categoryId\":1,\"targetExpense\":250000}]}");
        assertInvalidRequest("{\"year\":2026,\"month\":7,\"categories\":[]}");
        assertInvalidRequest("{\"year\":2026,\"month\":7,\"categories\":[{\"targetExpense\":250000}]}");
        assertInvalidRequest("{\"year\":2026,\"month\":7,\"categories\":[{\"categoryId\":0,\"targetExpense\":250000}]}");
        assertInvalidRequest("{\"year\":2026,\"month\":7,\"categories\":[{\"categoryId\":1}]}");
        assertInvalidRequest("{\"year\":2026,\"month\":7,\"categories\":[{\"categoryId\":1,\"targetExpense\":-1}]}");

        verifyNoInteractions(expenseSimulationService);
    }

    @Test
    void simulate_returnsGlobalErrorForServiceBusinessException() throws Exception {
        given(expenseSimulationService.simulate(eq(USER_ID), org.mockito.ArgumentMatchers.any(ExpenseSimulationRequest.class)))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE));

        mockMvc.perform(post("/api/expense-analysis/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));

        verify(expenseSimulationService).simulate(eq(USER_ID), org.mockito.ArgumentMatchers.any(ExpenseSimulationRequest.class));
    }

    @Test
    void simulate_withoutAuthenticationDoesNotCallServiceInStandaloneTest() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/expense-analysis/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(expenseSimulationService);
    }

    private void assertInvalidRequest(String requestBody) throws Exception {
        mockMvc.perform(post("/api/expense-analysis/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }
}
