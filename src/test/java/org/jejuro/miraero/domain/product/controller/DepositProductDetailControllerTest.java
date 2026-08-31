package org.jejuro.miraero.domain.product.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jejuro.miraero.domain.product.dto.response.DepositOptionResponse;
import org.jejuro.miraero.domain.product.dto.response.DepositProductDetailResponse;
import org.jejuro.miraero.domain.product.service.DepositProductService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DepositProductDetailControllerTest {

    @Mock
    private DepositProductService depositProductService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DepositProductController(depositProductService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getDepositProductDetail_returnsDetailResponse() throws Exception {
        given(depositProductService.getDepositProductDetail(1L)).willReturn(response());

        mockMvc.perform(get("/api/deposits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.joinRestriction").value("2"))
                .andExpect(jsonPath("$.data.hasJoinRestriction").value(true))
                .andExpect(jsonPath("$.data.productPageUrl").value("https://bank.example.com/deposit-a"))
                .andExpect(jsonPath("$.data.options[0].saveTerm").value(12))
                .andExpect(jsonPath("$.data.options[0].interestRateType").value("단리"));

        verify(depositProductService).getDepositProductDetail(1L);
    }

    @Test
    void getDepositProductDetail_returnsNotFoundError() throws Exception {
        given(depositProductService.getDepositProductDetail(999L))
                .willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

        mockMvc.perform(get("/api/deposits/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));
    }

    @Test
    void getDepositProductDetail_returnsInvalidInputError() throws Exception {
        given(depositProductService.getDepositProductDetail(0L))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE));

        mockMvc.perform(get("/api/deposits/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    private DepositProductDetailResponse response() {
        return new DepositProductDetailResponse(
                1L,
                "Bank A",
                "Deposit A",
                "Internet",
                "Individual",
                "2",
                true,
                "Special condition",
                "Maturity interest",
                10_000_000L,
                "Notice",
                "202607",
                LocalDate.of(2026, 7, 1),
                null,
                "https://bank.example.com/deposit-a",
                List.of(new DepositOptionResponse(
                        1L,
                        "단리",
                        12,
                        new BigDecimal("2.80"),
                        new BigDecimal("3.10")
                ))
        );
    }
}
