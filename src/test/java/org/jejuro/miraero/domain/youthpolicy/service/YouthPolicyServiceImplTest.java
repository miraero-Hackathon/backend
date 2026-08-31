package org.jejuro.miraero.domain.youthpolicy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.time.LocalDate;
import java.util.List;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyListQueryResult;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyListResponse;
import org.jejuro.miraero.domain.youthpolicy.mapper.YouthPolicyMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YouthPolicyServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final int AGE = 25;
    private static final Long MONTHLY_INCOME = 3_000_000L;
    private static final Long ANNUAL_INCOME = MONTHLY_INCOME * 12;

    @Mock
    private YouthPolicyMapper youthPolicyMapper;

    @Mock
    private UserMapper userMapper;

    private YouthPolicyService youthPolicyService;

    @BeforeEach
    void setUp() {
        youthPolicyService = new YouthPolicyServiceImpl(youthPolicyMapper, userMapper);
        lenient().when(userMapper.findById(USER_ID)).thenReturn(User.create(
                "Test user",
                LocalDate.now().minusYears(AGE),
                null,
                MONTHLY_INCOME,
                "test@example.com",
                "password",
                null
        ));
    }

    @Test
    void getYouthPolicies_returnsActivePoliciesWithoutEligibilityFilter() {
        LocalDate applicationEndDate = LocalDate.now().plusDays(7);
        when(youthPolicyMapper.findYouthPolicies(
                null, null, null, 0L, 10
        )).thenReturn(List.of(listResult(applicationEndDate)));
        when(youthPolicyMapper.countYouthPolicies(
                null, null, null
        )).thenReturn(1L);

        PageResponse<YouthPolicyListResponse> response = youthPolicyService
                .getYouthPolicies(USER_ID, null, null, null, 1, 10);

        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals(7L, response.getContent().get(0).getDDay());
        verify(youthPolicyMapper).findYouthPolicies(
                null, null, null, 0L, 10
        );
        verify(youthPolicyMapper).countYouthPolicies(
                null, null, null
        );
    }

    @Test
    void getRecommendedYouthPolicies_filtersUsingAgeAndIncome() {
        LocalDate applicationEndDate = LocalDate.now().plusDays(7);
        when(youthPolicyMapper.findRecommendedYouthPolicies(AGE, ANNUAL_INCOME, null, 3))
                .thenReturn(List.of(listResult(applicationEndDate)));
        when(youthPolicyMapper.countRecommendedYouthPolicies(AGE, ANNUAL_INCOME, null)).thenReturn(1L);

        PageResponse<YouthPolicyListResponse> response = youthPolicyService
                .getRecommendedYouthPolicies(USER_ID, null);

        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        verify(youthPolicyMapper).findRecommendedYouthPolicies(AGE, ANNUAL_INCOME, null, 3);
        verify(youthPolicyMapper).countRecommendedYouthPolicies(AGE, ANNUAL_INCOME, null);
    }

    @Test
    void getRecommendedYouthPolicies_filtersBySelectedRegion() {
        when(youthPolicyMapper.findRecommendedYouthPolicies(AGE, ANNUAL_INCOME, "11", 3))
                .thenReturn(List.of());
        when(youthPolicyMapper.countRecommendedYouthPolicies(AGE, ANNUAL_INCOME, "11"))
                .thenReturn(0L);

        youthPolicyService.getRecommendedYouthPolicies(USER_ID, "서울특별시");

        verify(youthPolicyMapper).findRecommendedYouthPolicies(AGE, ANNUAL_INCOME, "11", 3);
        verify(youthPolicyMapper).countRecommendedYouthPolicies(AGE, ANNUAL_INCOME, "11");
    }

    @Test
    void getYouthPolicies_convertsRegionNameToSidoCode() {
        when(youthPolicyMapper.findYouthPolicies(
                null, "11", null, 0L, 10
        )).thenReturn(List.of());
        when(youthPolicyMapper.countYouthPolicies(null, "11", null)).thenReturn(0L);

        youthPolicyService.getYouthPolicies(USER_ID, null, "서울특별시", null, 1, 10);

        verify(youthPolicyMapper).findYouthPolicies(null, "11", null, 0L, 10);
        verify(youthPolicyMapper).countYouthPolicies(null, "11", null);
    }

    @Test
    void getYouthPolicies_rejectsInvalidPageBeforeQuerying() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> youthPolicyService.getYouthPolicies(USER_ID, null, null, null, 0, 10)
        );

        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verifyNoInteractions(userMapper, youthPolicyMapper);
    }

    private YouthPolicyListQueryResult listResult(LocalDate applicationEndDate) {
        return new YouthPolicyListQueryResult(
                1L,
                "Policy",
                "Keyword",
                "Institution",
                LocalDate.now(),
                applicationEndDate,
                null
        );
    }
}
