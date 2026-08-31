package org.jejuro.miraero.domain.product.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jejuro.miraero.domain.product.dto.response.SavingOptionResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductDetailResponse;
import org.jejuro.miraero.domain.product.service.SavingProductService;
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
class SavingProductDetailControllerTest {

    @Mock
    private SavingProductService savingProductService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SavingProductController(savingProductService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getSavingProductDetail_returnsDetailResponse() throws Exception {
        given(savingProductService.getSavingProductDetail(1L)).willReturn(response());

        mockMvc.perform(get("/api/savings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.joinRestriction").value("2"))
                .andExpect(jsonPath("$.data.hasJoinRestriction").value(true))
                .andExpect(jsonPath("$.data.productPageUrl").value("https://bank.example.com/saving-a"))
                .andExpect(jsonPath("$.data.options[0].saveTerm").value(12))
                .andExpect(jsonPath("$.data.options[0].interestRateType").value("단리"))
                .andExpect(jsonPath("$.data.options[0].reserveType").value("정액적립식"));

        verify(savingProductService).getSavingProductDetail(1L);
    }

    @Test
    void getSavingProductDetail_returnsNotFoundError() throws Exception {
        given(savingProductService.getSavingProductDetail(999L))
                .willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

        mockMvc.perform(get("/api/savings/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));
    }

    @Test
    void getSavingProductDetail_returnsInvalidInputError() throws Exception {
        given(savingProductService.getSavingProductDetail(0L))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE));

        mockMvc.perform(get("/api/savings/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    private SavingProductDetailResponse response() {
        return new SavingProductDetailResponse(
                1L, "Bank A", "Saving A", "Internet", "Individual", "2", true,
                "Special condition", "Maturity interest", 500_000L, "Notice", "202607",
                LocalDate.of(2026, 7, 1), null,
                "https://bank.example.com/saving-a",
                List.of(new SavingOptionResponse(1L, "단리", "정액적립식", 12,
                        new BigDecimal("2.80"), new BigDecimal("3.10")))
        );
    }
}
