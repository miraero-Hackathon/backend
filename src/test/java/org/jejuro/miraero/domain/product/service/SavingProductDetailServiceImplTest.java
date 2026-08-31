package org.jejuro.miraero.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.product.domain.SavingOption;
import org.jejuro.miraero.domain.product.domain.SavingProductDetailQueryResult;
import org.jejuro.miraero.domain.product.dto.response.SavingProductDetailResponse;
import org.jejuro.miraero.domain.product.mapper.SavingProductMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingProductDetailServiceImplTest {

    @Mock
    private SavingProductMapper savingProductMapper;

    private SavingProductService savingProductService;

    @BeforeEach
    void setUp() {
        savingProductService = new SavingProductServiceImpl(savingProductMapper);
    }

    @Test
    void getSavingProductDetail_returnsProductAndConvertsAllOptionCodes() {
        when(savingProductMapper.findSavingProductDetailById(1L)).thenReturn(product());
        when(savingProductMapper.findSavingOptionsBySavingProductId(1L)).thenReturn(List.of(
                option(1L, "S", "F", 6, "2.50", "2.80"),
                option(2L, "M", "S", 6, "2.60", "2.90"),
                option(3L, "S", "S", 12, "2.80", "3.10")
        ));

        SavingProductDetailResponse response = savingProductService.getSavingProductDetail(1L);

        assertEquals("Bank A", response.getFinancialInstitutionName());
        assertEquals("https://bank.example.com/saving-a", response.getProductPageUrl());
        assertEquals("2", response.getJoinRestriction());
        assertEquals(true, response.getHasJoinRestriction());
        assertEquals(3, response.getOptions().size());
        assertEquals("단리", response.getOptions().get(0).getInterestRateType());
        assertEquals("자유적립식", response.getOptions().get(0).getReserveType());
        assertEquals("복리", response.getOptions().get(1).getInterestRateType());
        assertEquals("정액적립식", response.getOptions().get(1).getReserveType());
        assertEquals(12, response.getOptions().get(2).getSaveTerm());
        assertEquals(new BigDecimal("3.10"), response.getOptions().get(2).getMaxInterestRate());
        verify(savingProductMapper).findSavingProductDetailById(1L);
        verify(savingProductMapper).findSavingOptionsBySavingProductId(1L);
    }

    @Test
    void getSavingProductDetail_returnsEmptyOptionsWhenNoneExist() {
        when(savingProductMapper.findSavingProductDetailById(1L)).thenReturn(product());
        when(savingProductMapper.findSavingOptionsBySavingProductId(1L)).thenReturn(Collections.emptyList());

        SavingProductDetailResponse response = savingProductService.getSavingProductDetail(1L);

        assertEquals(0, response.getOptions().size());
    }

    @Test
    void getSavingProductDetail_throwsNotFoundWhenProductDoesNotExist() {
        when(savingProductMapper.findSavingProductDetailById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> savingProductService.getSavingProductDetail(999L)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getSavingProductDetail_rejectsInvalidId() {
        assertThrows(BusinessException.class, () -> savingProductService.getSavingProductDetail(null));
        assertThrows(BusinessException.class, () -> savingProductService.getSavingProductDetail(0L));

        verifyNoInteractions(savingProductMapper);
    }

    private SavingProductDetailQueryResult product() {
        return new SavingProductDetailQueryResult(
                1L, "Bank A", "Saving A", "Internet", "Individual", "2", true,
                "Special condition", "Maturity interest", 500_000L, "Notice", "202607",
                LocalDate.of(2026, 7, 1), null, "https://bank.example.com/saving-a"
        );
    }

    private SavingOption option(Long id, String interestRateType, String reserveType, Integer saveTerm,
                                String baseInterestRate, String maxInterestRate) {
        return new SavingOption(id, 1L, interestRateType, reserveType, saveTerm,
                new BigDecimal(baseInterestRate), new BigDecimal(maxInterestRate), null, null);
    }
}
