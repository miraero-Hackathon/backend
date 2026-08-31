package org.jejuro.miraero.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.transaction.domain.ExpenseCategoryTargetQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetItemRequest;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetSaveRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryTargetListResponse;
import org.jejuro.miraero.domain.transaction.mapper.ExpenseCategoryTargetMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryTargetServiceImplTest {

    private static final Long USER_ID = 42L;

    @Mock
    private ExpenseCategoryTargetMapper expenseCategoryTargetMapper;

    private ExpenseCategoryTargetService service;

    @BeforeEach
    void setUp() {
        service = new ExpenseCategoryTargetServiceImpl(expenseCategoryTargetMapper);
    }

    @Test
    void getTargets_returnsAllCategoriesAndKeepsUnsetTargetAsNull() {
        when(expenseCategoryTargetMapper.findTargetsByUserId(USER_ID)).thenReturn(Arrays.asList(
                result(1L, "식비", 300000L),
                result(2L, "카페", null)
        ));

        ExpenseCategoryTargetListResponse response = service.getTargets(USER_ID);

        assertEquals(2, response.getTargets().size());
        assertEquals("식비", response.getTargets().get(0).getCategoryName());
        assertEquals(300000L, response.getTargets().get(0).getTargetAmount());
        assertNull(response.getTargets().get(1).getTargetAmount());
        verify(expenseCategoryTargetMapper).findTargetsByUserId(USER_ID);
    }

    @Test
    void saveTargets_insertsSingleCategoryAndReturnsLatestTarget() {
        when(expenseCategoryTargetMapper.countExistingCategoriesByIds(List.of(1L))).thenReturn(1L);
        when(expenseCategoryTargetMapper.findTargetsByUserIdAndCategoryIds(USER_ID, List.of(1L)))
                .thenReturn(List.of(result(1L, "식비", 280000L)));

        ExpenseCategoryTargetListResponse response = service.saveTargets(
                USER_ID,
                request(item(1L, 280000L))
        );

        assertEquals(280000L, response.getTargets().get(0).getTargetAmount());
        verify(expenseCategoryTargetMapper).upsertTargets(eq(USER_ID), any());
        verify(expenseCategoryTargetMapper).findTargetsByUserIdAndCategoryIds(USER_ID, List.of(1L));
    }

    @Test
    void saveTargets_updatesExistingTargetWithSameUpsertOperation() {
        when(expenseCategoryTargetMapper.countExistingCategoriesByIds(List.of(1L))).thenReturn(1L);
        when(expenseCategoryTargetMapper.findTargetsByUserIdAndCategoryIds(USER_ID, List.of(1L)))
                .thenReturn(List.of(result(1L, "식비", 310000L)));

        ExpenseCategoryTargetListResponse response = service.saveTargets(
                USER_ID,
                request(item(1L, 310000L))
        );

        assertEquals(310000L, response.getTargets().get(0).getTargetAmount());
        verify(expenseCategoryTargetMapper).upsertTargets(eq(USER_ID), any());
    }

    @Test
    void saveTargets_savesMultipleCategoriesAndReturnsOnlyRequestedCategories() {
        List<Long> categoryIds = Arrays.asList(1L, 2L);
        when(expenseCategoryTargetMapper.countExistingCategoriesByIds(categoryIds)).thenReturn(2L);
        when(expenseCategoryTargetMapper.findTargetsByUserIdAndCategoryIds(USER_ID, categoryIds))
                .thenReturn(Arrays.asList(result(1L, "식비", 280000L), result(2L, "카페", 50000L)));

        ExpenseCategoryTargetListResponse response = service.saveTargets(
                USER_ID,
                request(item(1L, 280000L), item(2L, 50000L))
        );

        assertEquals(2, response.getTargets().size());
        ArgumentCaptor<List<ExpenseCategoryTargetItemRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(expenseCategoryTargetMapper).upsertTargets(eq(USER_ID), captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals(2L, captor.getValue().get(1).getCategoryId());
        verify(expenseCategoryTargetMapper)
                .findTargetsByUserIdAndCategoryIds(USER_ID, categoryIds);
    }

    @Test
    void saveTargets_doesNotIncludeTargetsNotPresentInRequest() {
        when(expenseCategoryTargetMapper.countExistingCategoriesByIds(List.of(1L))).thenReturn(1L);
        when(expenseCategoryTargetMapper.findTargetsByUserIdAndCategoryIds(USER_ID, List.of(1L)))
                .thenReturn(List.of(result(1L, "식비", 280000L)));

        service.saveTargets(USER_ID, request(item(1L, 280000L)));

        ArgumentCaptor<List<ExpenseCategoryTargetItemRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(expenseCategoryTargetMapper).upsertTargets(eq(USER_ID), captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(1L, captor.getValue().get(0).getCategoryId());
    }

    @Test
    void saveTargets_allowsZeroTargetAmount() {
        when(expenseCategoryTargetMapper.countExistingCategoriesByIds(List.of(1L))).thenReturn(1L);
        when(expenseCategoryTargetMapper.findTargetsByUserIdAndCategoryIds(USER_ID, List.of(1L)))
                .thenReturn(List.of(result(1L, "식비", 0L)));

        ExpenseCategoryTargetListResponse response = service.saveTargets(USER_ID, request(item(1L, 0L)));

        assertEquals(0L, response.getTargets().get(0).getTargetAmount());
    }

    @Test
    void saveTargets_rejectsEmptyAndDuplicateTargets() {
        assertInvalid(request());
        assertInvalid(request(item(1L, 100L), item(1L, 200L)));

        verifyNoInteractions(expenseCategoryTargetMapper);
    }

    @Test
    void saveTargets_rejectsMissingCategory() {
        when(expenseCategoryTargetMapper.countExistingCategoriesByIds(List.of(999L))).thenReturn(0L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.saveTargets(USER_ID, request(item(999L, 100L)))
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void rejectsInvalidUserId() {
        assertInvalidUserId(null);
        assertInvalidUserId(0L);
        assertInvalidUserId(-1L);

        verifyNoInteractions(expenseCategoryTargetMapper);
    }

    private void assertInvalid(ExpenseCategoryTargetSaveRequest request) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.saveTargets(USER_ID, request)
        );
        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    private void assertInvalidUserId(Long userId) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getTargets(userId)
        );
        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    private ExpenseCategoryTargetSaveRequest request(ExpenseCategoryTargetItemRequest... targets) {
        return ExpenseCategoryTargetSaveRequest.builder()
                .targets(targets == null ? null : Arrays.asList(targets))
                .build();
    }

    private ExpenseCategoryTargetItemRequest item(Long categoryId, Long targetAmount) {
        return ExpenseCategoryTargetItemRequest.builder()
                .categoryId(categoryId)
                .targetAmount(targetAmount)
                .build();
    }

    private ExpenseCategoryTargetQueryResult result(
            Long categoryId,
            String categoryName,
            Long targetAmount
    ) {
        return new ExpenseCategoryTargetQueryResult(categoryId, categoryName, targetAmount);
    }
}
