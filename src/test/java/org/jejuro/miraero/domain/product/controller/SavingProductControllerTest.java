package org.jejuro.miraero.domain.product.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.jejuro.miraero.domain.product.dto.response.SavingProductListResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductResponse;
import org.jejuro.miraero.domain.product.service.SavingProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SavingProductControllerTest {

    @Mock
    private SavingProductService savingProductService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SavingProductController(savingProductService)).build();
    }

    @Test
    void getSavingProducts_returnsProductCardsWithDisplayNames() throws Exception {
        given(savingProductService.getSavingProducts()).willReturn(new SavingProductListResponse(List.of(
                new SavingProductResponse(
                        1L,
                        "Bank A",
                        "Saving A",
                        new BigDecimal("4.50"),
                        List.of(6, 12),
                        List.of("정액적립식", "자유적립식"),
                        500_000L,
                        "Internet",
                        false,
                        true
                )
        )));

        mockMvc.perform(get("/api/savings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.products[0].savingProductId").value(1))
                .andExpect(jsonPath("$.data.products[0].reserveTypes[0]").value("정액적립식"))
                .andExpect(jsonPath("$.data.products[0].reserveTypes[1]").value("자유적립식"));

        verify(savingProductService).getSavingProducts();
    }
}
