package org.jejuro.miraero.domain.product.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.jejuro.miraero.domain.product.dto.response.DepositProductListResponse;
import org.jejuro.miraero.domain.product.dto.response.DepositProductResponse;
import org.jejuro.miraero.domain.product.service.DepositProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DepositProductControllerTest {

    @Mock
    private DepositProductService depositProductService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DepositProductController(depositProductService)).build();
    }

    @Test
    void getDepositProducts_returnsProductCards() throws Exception {
        given(depositProductService.getDepositProducts()).willReturn(new DepositProductListResponse(List.of(
                new DepositProductResponse(
                        1L,
                        "Bank A",
                        "Deposit A",
                        new BigDecimal("4.10"),
                        List.of(6, 12),
                        10_000_000L,
                        "Internet",
                        true,
                        true
                )
        )));

        mockMvc.perform(get("/api/deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.products[0].depositProductId").value(1))
                .andExpect(jsonPath("$.data.products[0].maxInterestRate").value(4.10))
                .andExpect(jsonPath("$.data.products[0].saveTerms[0]").value(6))
                .andExpect(jsonPath("$.data.products[0].saveTerms[1]").value(12))
                .andExpect(jsonPath("$.data.products[0].hasSpecialCondition").value(true));

        verify(depositProductService).getDepositProducts();
    }
}
