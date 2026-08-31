package org.jejuro.miraero.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.transaction.domain.TransactionQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    private static final Long USER_ID = 10L;

    @Mock
    private TransactionMapper transactionMapper;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionMapper);
    }

    @Test
    @DisplayName("페이지 정보와 월 조회 범위를 계산한다")
    void getTransactions_calculatesPaginationAndDateRange() {
        TransactionSearchCondition condition = createCondition(null, 2, 10);
        when(transactionMapper.findTransactions(eq(USER_ID), eq(condition)))
                .thenReturn(List.of(transaction(2L, LocalDateTime.of(2026, 7, 30, 12, 30))));
        when(transactionMapper.countTransactions(eq(USER_ID), eq(condition))).thenReturn(35L);

        PageResponse<?> response = transactionService.getTransactions(USER_ID, condition);

        assertEquals(1, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(35L, response.getTotalElements());
        assertEquals(4, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), condition.getStartDateTime());
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), condition.getEndDateTime());
        assertEquals(10, condition.getOffset());
    }

    @Test
    @DisplayName("categoryId를 포함한 검색 조건을 Mapper에 전달한다")
    void getTransactions_passesCategoryIdToMapper() {
        TransactionSearchCondition condition = createCondition(3L, 1, 10);
        when(transactionMapper.findTransactions(USER_ID, condition)).thenReturn(Collections.emptyList());
        when(transactionMapper.countTransactions(USER_ID, condition)).thenReturn(0L);

        transactionService.getTransactions(USER_ID, condition);

        ArgumentCaptor<TransactionSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(TransactionSearchCondition.class);
        verify(transactionMapper).findTransactions(eq(USER_ID), conditionCaptor.capture());
        verify(transactionMapper).countTransactions(eq(USER_ID), eq(condition));
        assertEquals(3L, conditionCaptor.getValue().getCategoryId());
    }

    @Test
    @DisplayName("조회 거래가 없으면 빈 목록과 0건 페이지 정보를 반환한다")
    void getTransactions_returnsEmptyList() {
        TransactionSearchCondition condition = createCondition(null, 1, 10);
        when(transactionMapper.findTransactions(USER_ID, condition)).thenReturn(Collections.emptyList());
        when(transactionMapper.countTransactions(USER_ID, condition)).thenReturn(0L);

        PageResponse<?> response = transactionService.getTransactions(USER_ID, condition);

        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(0L, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    @DisplayName("마지막 페이지 조회 시 공통 페이지 응답의 last 값이 true다")
    void getTransactions_marksLastPage() {
        TransactionSearchCondition condition = createCondition(null, 4, 10);
        when(transactionMapper.findTransactions(USER_ID, condition)).thenReturn(Collections.emptyList());
        when(transactionMapper.countTransactions(USER_ID, condition)).thenReturn(35L);

        PageResponse<?> response = transactionService.getTransactions(USER_ID, condition);

        assertEquals(3, response.getPage());
        assertTrue(response.isLast());
    }

    @Test
    @DisplayName("전달받은 userId를 목록과 COUNT Mapper 호출에 동일하게 사용한다")
    void getTransactions_passesUserIdToBothMapperCalls() {
        Long anotherUserId = 25L;
        TransactionSearchCondition condition = createCondition(null, 1, 10);
        when(transactionMapper.findTransactions(anotherUserId, condition)).thenReturn(Collections.emptyList());
        when(transactionMapper.countTransactions(anotherUserId, condition)).thenReturn(0L);

        transactionService.getTransactions(anotherUserId, condition);

        verify(transactionMapper).findTransactions(anotherUserId, condition);
        verify(transactionMapper).countTransactions(anotherUserId, condition);
    }

    @Test
    @DisplayName("유효하지 않은 userId, month, page, size는 입력값 예외를 발생시킨다")
    void getTransactions_rejectsInvalidValues() {
        assertInvalid(null, createCondition(null, 1, 10));
        assertInvalid(0L, createCondition(null, 1, 10));
        assertInvalid(USER_ID, TransactionSearchCondition.builder()
                .year(2026).month(13).page(1).size(10).build());
        assertInvalid(USER_ID, TransactionSearchCondition.builder()
                .year(2026).month(7).page(0).size(10).build());
        assertInvalid(USER_ID, TransactionSearchCondition.builder()
                .year(2026).month(7).page(1).size(101).build());
    }

    private TransactionSearchCondition createCondition(Long categoryId, int page, int size) {
        return TransactionSearchCondition.builder()
                .year(2026)
                .month(7)
                .categoryId(categoryId)
                .page(page)
                .size(size)
                .build();
    }

    private TransactionQueryResult transaction(Long transactionId, LocalDateTime transactedAt) {
        return new TransactionQueryResult(
                transactionId,
                "PAYMENT",
                "Miraero Cafe",
                15_000L,
                1_200_000L,
                transactedAt,
                1L,
                "식비"
        );
    }

    private void assertInvalid(Long userId, TransactionSearchCondition condition) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transactionService.getTransactions(userId, condition)
        );

        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

}
