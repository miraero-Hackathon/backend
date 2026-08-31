package org.jejuro.miraero.domain.transaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetSaveRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryTargetListResponse;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryTargetResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseCategoryTargetService;
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
class ExpenseCategoryTargetControllerTest {

    private static final Long USER_ID = 42L;

    @Mock
    private ExpenseCategoryTargetService expenseCategoryTargetService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExpenseCategoryTargetController(expenseCategoryTargetService))
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
    void getTargets_returnsApiResponseAndAuthenticatedUserTargets() throws Exception {
        given(expenseCategoryTargetService.getTargets(USER_ID)).willReturn(response());

        mockMvc.perform(get("/api/expense-category-targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targets[0].categoryName").value("식비"))
                .andExpect(jsonPath("$.data.targets[1].targetAmount").doesNotExist());

        verify(expenseCategoryTargetService).getTargets(USER_ID);
    }

    @Test
    void saveTargets_acceptsSingleAndMultipleTargetsAndUsesAuthenticatedUser() throws Exception {
        given(expenseCategoryTargetService.saveTargets(eq(USER_ID), any(ExpenseCategoryTargetSaveRequest.class)))
                .willReturn(response());

        mockMvc.perform(put("/api/expense-category-targets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targets\":[{\"categoryId\":1,\"targetAmount\":280000}],\"userId\":999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targets[0].targetAmount").value(280000));

        mockMvc.perform(put("/api/expense-category-targets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targets\":[{\"categoryId\":1,\"targetAmount\":280000},{\"categoryId\":2,\"targetAmount\":50000}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<ExpenseCategoryTargetSaveRequest> captor =
                ArgumentCaptor.forClass(ExpenseCategoryTargetSaveRequest.class);
        verify(expenseCategoryTargetService, org.mockito.Mockito.times(2))
                .saveTargets(eq(USER_ID), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(1, captor.getAllValues().get(0).getTargets().size());
        org.junit.jupiter.api.Assertions.assertEquals(
                2,
                captor.getAllValues().get(1).getTargets().size()
        );
    }

    @Test
    void saveTargets_rejectsBeanValidationFailureWithoutCallingService() throws Exception {
        mockMvc.perform(put("/api/expense-category-targets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targets\":[{\"categoryId\":1,\"targetAmount\":-1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));

        verifyNoInteractions(expenseCategoryTargetService);
    }

    private ExpenseCategoryTargetListResponse response() {
        return new ExpenseCategoryTargetListResponse(Arrays.asList(
                new ExpenseCategoryTargetResponse(1L, "식비", 280000L),
                new ExpenseCategoryTargetResponse(2L, "카페", null)
        ));
    }
}
