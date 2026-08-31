package org.jejuro.miraero.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.jejuro.miraero.domain.product.domain.DepositProductListQueryResult;
import org.jejuro.miraero.domain.product.dto.response.DepositProductListResponse;
import org.jejuro.miraero.domain.product.mapper.DepositProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepositProductServiceImplTest {

    @Mock
    private DepositProductMapper depositProductMapper;

    private DepositProductService depositProductService;

    @BeforeEach
    void setUp() {
        depositProductService = new DepositProductServiceImpl(depositProductMapper);
    }

    @Test
    void getDepositProducts_groupsSaveTermsByProductInQueryOrder() {
        when(depositProductMapper.findDepositProductList()).thenReturn(List.of(
                result(1L, "Bank A", "Deposit A", "4.10", 6, true, true),
                result(1L, "Bank A", "Deposit A", "4.10", 12, true, true),
                result(2L, "Bank B", "Deposit B", "3.80", 12, false, false)
        ));

        DepositProductListResponse response = depositProductService.getDepositProducts();

        assertEquals(2, response.getProducts().size());
        assertEquals(1L, response.getProducts().get(0).getDepositProductId());
        assertEquals(List.of(6, 12), response.getProducts().get(0).getSaveTerms());
        assertEquals(new BigDecimal("4.10"), response.getProducts().get(0).getMaxInterestRate());
        assertEquals(true, response.getProducts().get(0).getHasJoinRestriction());
        assertEquals(true, response.getProducts().get(0).getHasSpecialCondition());
        assertEquals(List.of(12), response.getProducts().get(1).getSaveTerms());
        assertEquals(false, response.getProducts().get(1).getHasJoinRestriction());
        assertEquals(false, response.getProducts().get(1).getHasSpecialCondition());
        verify(depositProductMapper).findDepositProductList();
    }

    private DepositProductListQueryResult result(
            Long depositProductId,
            String financialInstitutionName,
            String productName,
            String maxInterestRate,
            Integer saveTerm,
            Boolean hasJoinRestriction,
            Boolean hasSpecialCondition
    ) {
        return new DepositProductListQueryResult(
                depositProductId,
                financialInstitutionName,
                productName,
                new BigDecimal(maxInterestRate),
                saveTerm,
                10_000_000L,
                "Internet",
                hasJoinRestriction,
                hasSpecialCondition
        );
    }
}
