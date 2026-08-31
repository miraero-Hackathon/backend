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
import org.jejuro.miraero.domain.product.domain.DepositOption;
import org.jejuro.miraero.domain.product.domain.DepositProductDetailQueryResult;
import org.jejuro.miraero.domain.product.dto.response.DepositProductDetailResponse;
import org.jejuro.miraero.domain.product.mapper.DepositProductMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepositProductDetailServiceImplTest {

    @Mock
    private DepositProductMapper depositProductMapper;

    private DepositProductService depositProductService;

    @BeforeEach
    void setUp() {
        depositProductService = new DepositProductServiceImpl(depositProductMapper);
    }

    @Test
    void getDepositProductDetail_returnsProductAndAllSortedOptions() {
        when(depositProductMapper.findDepositProductDetailById(1L)).thenReturn(product());
        when(depositProductMapper.findDepositOptionsByDepositProductId(1L)).thenReturn(List.of(
                option(1L, "S", 6, "2.50", "2.80"),
                option(2L, "M", 6, "2.60", "2.90"),
                option(3L, "S", 12, "2.80", "3.10")
        ));

        DepositProductDetailResponse response = depositProductService.getDepositProductDetail(1L);

        assertEquals("Bank A", response.getFinancialInstitutionName());
        assertEquals("https://bank.example.com/deposit-a", response.getProductPageUrl());
        assertEquals("2", response.getJoinRestriction());
        assertEquals(true, response.getHasJoinRestriction());
        assertEquals(3, response.getOptions().size());
        assertEquals("단리", response.getOptions().get(0).getInterestRateType());
        assertEquals("복리", response.getOptions().get(1).getInterestRateType());
        assertEquals(12, response.getOptions().get(2).getSaveTerm());
        assertEquals(new BigDecimal("3.10"), response.getOptions().get(2).getMaxInterestRate());
        verify(depositProductMapper).findDepositProductDetailById(1L);
        verify(depositProductMapper).findDepositOptionsByDepositProductId(1L);
    }

    @Test
    void getDepositProductDetail_returnsEmptyOptionsWhenNoneExist() {
        when(depositProductMapper.findDepositProductDetailById(1L)).thenReturn(product());
        when(depositProductMapper.findDepositOptionsByDepositProductId(1L)).thenReturn(Collections.emptyList());

        DepositProductDetailResponse response = depositProductService.getDepositProductDetail(1L);

        assertEquals(0, response.getOptions().size());
    }

    @Test
    void getDepositProductDetail_throwsNotFoundWhenProductDoesNotExist() {
        when(depositProductMapper.findDepositProductDetailById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositProductService.getDepositProductDetail(999L)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getDepositProductDetail_rejectsInvalidId() {
        assertThrows(BusinessException.class, () -> depositProductService.getDepositProductDetail(null));
        assertThrows(BusinessException.class, () -> depositProductService.getDepositProductDetail(0L));

        verifyNoInteractions(depositProductMapper);
    }

    private DepositProductDetailQueryResult product() {
        return new DepositProductDetailQueryResult(
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
                "https://bank.example.com/deposit-a"
        );
    }

    private DepositOption option(
            Long depositOptionId,
            String interestRateType,
            Integer saveTerm,
            String baseInterestRate,
            String maxInterestRate
    ) {
        return new DepositOption(
                depositOptionId,
                1L,
                interestRateType,
                saveTerm,
                new BigDecimal(baseInterestRate),
                new BigDecimal(maxInterestRate),
                null,
                null
        );
    }
}
