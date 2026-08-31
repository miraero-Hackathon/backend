package org.jejuro.miraero.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jejuro.miraero.domain.transaction.domain.PeerCategoryAverageQueryResult;
import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageResponse;
import org.jejuro.miraero.domain.transaction.mapper.PeerAverageMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PeerAverageServiceImplTest {

    @Mock
    private PeerAverageMapper peerAverageMapper;

    private PeerAverageService service;

    @BeforeEach
    void setUp() {
        service = new PeerAverageServiceImpl(peerAverageMapper);
    }

    @Test
    void getPeerAverages_returnsCategoryAveragesIncludingZeroAmount() {
        YearMonth currentMonth = YearMonth.now();
        when(peerAverageMapper.findCategoryPeerAverages(
                eq(1L),
                eq(currentMonth.atDay(1).atStartOfDay()),
                eq(currentMonth.plusMonths(1).atDay(1).atStartOfDay())
        )).thenReturn(List.of(
                new PeerCategoryAverageQueryResult(1L, "Food", 285_000L),
                new PeerCategoryAverageQueryResult(2L, "Cafe", 0L)
        ));

        PeerAverageResponse response = service.getPeerAverages(1L);

        assertEquals(2, response.getCategories().size());
        assertEquals(1L, response.getCategories().get(0).getCategoryId());
        assertEquals("Food", response.getCategories().get(0).getCategoryName());
        assertEquals(285_000L, response.getCategories().get(0).getPeerAverageAmount());
        assertEquals(0L, response.getCategories().get(1).getPeerAverageAmount());
    }

    @Test
    void getPeerAverages_usesCurrentMonthRange() {
        YearMonth currentMonth = YearMonth.now();
        when(peerAverageMapper.findCategoryPeerAverages(
                eq(1L),
                eq(currentMonth.atDay(1).atStartOfDay()),
                eq(currentMonth.plusMonths(1).atDay(1).atStartOfDay())
        )).thenReturn(List.of());

        service.getPeerAverages(1L);

        verify(peerAverageMapper).findCategoryPeerAverages(
                1L,
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.plusMonths(1).atDay(1).atStartOfDay()
        );
    }

    @Test
    void getPeerAverages_rejectsInvalidInput() {
        assertThrows(BusinessException.class, () -> service.getPeerAverages(null));
        assertThrows(BusinessException.class, () -> service.getPeerAverages(0L));

        verifyNoInteractions(peerAverageMapper);
    }
}
