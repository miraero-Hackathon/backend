package org.jejuro.miraero.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.jejuro.miraero.domain.product.domain.SavingProductListQueryResult;
import org.jejuro.miraero.domain.product.dto.response.SavingProductListResponse;
import org.jejuro.miraero.domain.product.mapper.SavingProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingProductServiceImplTest {

    @Mock
    private SavingProductMapper savingProductMapper;

    private SavingProductService savingProductService;

    @BeforeEach
    void setUp() {
        savingProductService = new SavingProductServiceImpl(savingProductMapper);
    }

    @Test
    void getSavingProducts_groupsTermsAndConvertsReserveTypesInEnumOrder() {
        when(savingProductMapper.findSavingProductList()).thenReturn(List.of(
                result(1L, "4.50", 6, "F", true, true),
                result(1L, "4.50", 6, "S", true, true),
                result(1L, "4.50", 12, "S", true, true),
                result(2L, "4.50", 12, "F", false, false),
                result(3L, "3.80", 24, "S", false, true)
        ));

        SavingProductListResponse response = savingProductService.getSavingProducts();

        assertEquals(List.of(1L, 2L, 3L), response.getProducts().stream()
                .map(product -> product.getSavingProductId())
                .collect(java.util.stream.Collectors.toList()));
        assertEquals(List.of(6, 12), response.getProducts().get(0).getSaveTerms());
        assertEquals(List.of("정액적립식", "자유적립식"), response.getProducts().get(0).getReserveTypes());
        assertEquals(List.of("자유적립식"), response.getProducts().get(1).getReserveTypes());
        assertEquals(new BigDecimal("4.50"), response.getProducts().get(0).getHighestInterestRate());
        assertEquals(true, response.getProducts().get(0).getHasJoinRestriction());
        assertEquals(false, response.getProducts().get(1).getHasSpecialCondition());
        verify(savingProductMapper).findSavingProductList();
    }

    @Test
    void getSavingProducts_returnsEmptyListWhenNoProductHasOptions() {
        when(savingProductMapper.findSavingProductList()).thenReturn(List.of());

        SavingProductListResponse response = savingProductService.getSavingProducts();

        assertEquals(0, response.getProducts().size());
    }

    private SavingProductListQueryResult result(
            Long savingProductId,
            String highestInterestRate,
            Integer saveTerm,
            String reserveType,
            Boolean hasJoinRestriction,
            Boolean hasSpecialCondition
    ) {
        return new SavingProductListQueryResult(
                savingProductId,
                "Bank " + savingProductId,
                "Saving " + savingProductId,
                new BigDecimal(highestInterestRate),
                saveTerm,
                reserveType,
                500_000L,
                "Internet",
                hasJoinRestriction,
                hasSpecialCondition
        );
    }
}
