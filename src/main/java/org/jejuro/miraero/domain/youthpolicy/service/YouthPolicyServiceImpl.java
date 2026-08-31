package org.jejuro.miraero.domain.youthpolicy.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyDetailQueryResult;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyListQueryResult;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyDetailResponse;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyListResponse;
import org.jejuro.miraero.domain.youthpolicy.mapper.YouthPolicyMapper;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class YouthPolicyServiceImpl implements YouthPolicyService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int RECOMMENDED_POLICY_SIZE = 3;
    private static final int MONTHS_PER_YEAR = 12;
    private static final String APPLICATION_PERIOD_BETWEEN_SEPARATOR = " ~ ";
    private static final String APPLICATION_PERIOD_START_ONLY_SUFFIX = " ~";
    private static final String APPLICATION_PERIOD_END_ONLY_PREFIX = "~ ";

    private final YouthPolicyMapper youthPolicyMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<YouthPolicyListResponse> getYouthPolicies(
            Long userId,
            String keyword,
            String region,
            String search,
            int page,
            int size
    ) {
        validatePage(page, size);
        String regionCode = resolveRegionCode(region);
        long offset = (long) (page - 1) * size;
        List<YouthPolicyListResponse> policies = youthPolicyMapper
                .findYouthPolicies(keyword, regionCode, search, offset, size)
                .stream()
                .map(this::toYouthPolicyListResponse)
                .collect(Collectors.toList());
        long totalElements = youthPolicyMapper.countYouthPolicies(
                keyword,
                regionCode,
                search
        );

        return PageResponse.of(policies, page - 1, size, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<YouthPolicyListResponse> getRecommendedYouthPolicies(Long userId, String region) {
        User user = getUserWithEligibilityInfo(userId);
        int age = calculateAge(user.getBirthDate());
        Long annualIncome = calculateAnnualIncome(user.getMonthlyIncome());
        String regionCode = resolveRegionCode(region);

        List<YouthPolicyListResponse> policies = youthPolicyMapper
                .findRecommendedYouthPolicies(age, annualIncome, regionCode, RECOMMENDED_POLICY_SIZE)
                .stream()
                .map(this::toYouthPolicyListResponse)
                .collect(Collectors.toList());
        long totalElements = youthPolicyMapper.countRecommendedYouthPolicies(
                age,
                annualIncome,
                regionCode
        );

        return PageResponse.of(policies, 0, RECOMMENDED_POLICY_SIZE, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public YouthPolicyDetailResponse getYouthPolicyDetail(Long youthPolicyId) {
        validateYouthPolicyId(youthPolicyId);

        YouthPolicyDetailQueryResult result = youthPolicyMapper.findYouthPolicyById(youthPolicyId);
        if (result == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        return toYouthPolicyDetailResponse(result);
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateYouthPolicyId(Long youthPolicyId) {
        if (youthPolicyId == null || youthPolicyId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String resolveRegionCode(String region) {
        String regionCode = YouthPolicyRegionResolver.resolveRegionCode(region);
        if (region != null && !region.isBlank() && regionCode == null) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
        return regionCode;
    }

    private YouthPolicyListResponse toYouthPolicyListResponse(YouthPolicyListQueryResult result) {
        return new YouthPolicyListResponse(
                result.getYouthPolicyId(),
                result.getPolicyName(),
                result.getPolicyKeyword(),
                result.getProviderInstitutionName(),
                createApplicationPeriod(
                        result.getApplicationStartDate(),
                        result.getApplicationEndDate(),
                        result.getApplicationPeriodText()
                ),
                calculateDDay(result.getApplicationEndDate())
        );
    }

    private YouthPolicyDetailResponse toYouthPolicyDetailResponse(YouthPolicyDetailQueryResult result) {
        return new YouthPolicyDetailResponse(
                result.getYouthPolicyId(),
                result.getPolicyName(),
                result.getPolicyKeyword(),
                result.getPolicyDescription(),
                result.getSupportContent(),
                result.getProviderInstitutionName(),
                result.getApplicationStartDate(),
                result.getApplicationEndDate(),
                createApplicationPeriod(
                        result.getApplicationStartDate(),
                        result.getApplicationEndDate(),
                        result.getApplicationPeriodText()
                ),
                result.getMinAge(),
                result.getMaxAge(),
                result.getMinIncome(),
                result.getMaxIncome(),
                result.getIncomeConditionText(),
                result.getQualification(),
                result.getApplicationMethod(),
                result.getApplicationUrl(),
                result.getReferenceUrl()
        );
    }

    private String createApplicationPeriod(
            LocalDate applicationStartDate,
            LocalDate applicationEndDate,
            String applicationPeriodText
    ) {
        if (applicationStartDate != null && applicationEndDate != null) {
            return formatDate(applicationStartDate)
                    + APPLICATION_PERIOD_BETWEEN_SEPARATOR
                    + formatDate(applicationEndDate);
        }
        if (applicationStartDate != null) {
            return formatDate(applicationStartDate) + APPLICATION_PERIOD_START_ONLY_SUFFIX;
        }
        if (applicationEndDate != null) {
            return APPLICATION_PERIOD_END_ONLY_PREFIX + formatDate(applicationEndDate);
        }
        if (applicationPeriodText != null && !applicationPeriodText.trim().isEmpty()) {
            return applicationPeriodText;
        }
        return null;
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private User getUserWithEligibilityInfo(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
        if (user.getBirthDate() == null || user.getMonthlyIncome() == null) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
        return user;
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private Long calculateAnnualIncome(Long monthlyIncome) {
        return monthlyIncome * MONTHS_PER_YEAR;
    }

    private Long calculateDDay(LocalDate applicationEndDate) {
        if (applicationEndDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), applicationEndDate);
    }
}
