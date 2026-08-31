package org.jejuro.miraero.domain.youthpolicy.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.youthpolicy.client.YouthPolicyApiClient;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicy;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyRegion;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiItem;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiPaging;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiResponse;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiResult;
import org.jejuro.miraero.domain.youthpolicy.mapper.YouthPolicyMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class YouthPolicySyncServiceImpl implements YouthPolicySyncService {

    private static final Logger log = LoggerFactory.getLogger(YouthPolicySyncServiceImpl.class);
    private static final DateTimeFormatter APPLICATION_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Pattern APPLICATION_PERIOD_PATTERN = Pattern.compile("^(\\d{8})\\s*~\\s*(\\d{8})$");
    private static final String QUALIFICATION_SEPARATOR = "\n";
    private static final List<String> TARGET_POLICY_KEYWORDS = List.of(
            "대출",
            "보조금",
            "바우처",
            "금리혜택",
            "신용회복",
            "공공임대주택",
            "주거지원"
    );

    private final YouthPolicyApiClient youthPolicyApiClient;
    private final YouthPolicyMapper youthPolicyMapper;

    @Override
    @Transactional
    public void syncYouthPolicy(YouthPolicyApiItem source) {
        youthPolicyMapper.upsert(toYouthPolicy(source));
        Long youthPolicyId = youthPolicyMapper.findYouthPolicyIdByPolicyNo(source.getPlcyNo());
        if (youthPolicyId == null) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        youthPolicyMapper.deleteRegionsByYouthPolicyId(youthPolicyId);
        List<YouthPolicyRegion> regions = YouthPolicyRegionResolver.resolve(source.getZipCd());
        if (!regions.isEmpty()) {
            youthPolicyMapper.insertRegions(youthPolicyId, regions);
        }
    }

    @Override
    public void syncYouthPolicies() {
        log.info("금융·주거 청년정책 동기화를 시작합니다.");

        try {
            int syncedCount = 0;

            for (String policyKeyword : TARGET_POLICY_KEYWORDS) {
                syncedCount += syncYouthPoliciesByKeyword(policyKeyword);
            }

            log.info("금융·주거 청년정책 동기화를 완료했습니다. 저장 건수: {}", syncedCount);
        } catch (RuntimeException exception) {
            log.error("금융·주거 청년정책 동기화 중 오류가 발생했습니다.", exception);
            throw exception;
        }
    }

    private int syncYouthPoliciesByKeyword(String policyKeyword) {
        YouthPolicyApiResult firstResult = getYouthPolicyApiResult(1, policyKeyword);
        int totalPages = calculateTotalPages(firstResult.getPagging());
        int syncedCount = syncYouthPolicyItems(firstResult.getYouthPolicyList());

        for (int pageNum = 2; pageNum <= totalPages; pageNum++) {
            YouthPolicyApiResult result = getYouthPolicyApiResult(pageNum, policyKeyword);
            syncedCount += syncYouthPolicyItems(result.getYouthPolicyList());
        }

        return syncedCount;
    }

    private YouthPolicyApiResult getYouthPolicyApiResult(int pageNum, String policyKeyword) {
        YouthPolicyApiResponse response = youthPolicyApiClient.getYouthPolicies(pageNum, policyKeyword);
        if (response == null || response.getResult() == null
                || response.getResult().getPagging() == null
                || response.getResult().getYouthPolicyList() == null) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        return response.getResult();
    }

    private int calculateTotalPages(YouthPolicyApiPaging paging) {
        Integer totalCount = paging.getTotCount();
        Integer pageSize = paging.getPageSize();

        if (totalCount == null || totalCount < 0 || pageSize == null || pageSize <= 0) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        return (int) Math.ceil((double) totalCount / pageSize);
    }

    private int syncYouthPolicyItems(List<YouthPolicyApiItem> youthPolicyItems) {
        int syncedCount = 0;

        for (YouthPolicyApiItem youthPolicyItem : youthPolicyItems) {
            if (youthPolicyItem == null) {
                continue;
            }
            syncYouthPolicy(youthPolicyItem);
            syncedCount++;
        }
        return syncedCount;
    }

    private YouthPolicy toYouthPolicy(YouthPolicyApiItem source) {
        ApplicationPeriod applicationPeriod = parseApplicationPeriod(source.getAplyYmd());

        return YouthPolicy.builder()
                .policyNo(source.getPlcyNo())
                .policyName(source.getPlcyNm())
                .policyKeyword(source.getPlcyKywdNm())
                .policyDescription(source.getPlcyExplnCn())
                .supportContent(source.getPlcySprtCn())
                .providerInstitutionCode(source.getSprvsnInstCd())
                .providerInstitutionName(source.getSprvsnInstCdNm())
                .zipCd(source.getZipCd())
                .applicationStartDate(applicationPeriod.getApplicationStartDate())
                .applicationEndDate(applicationPeriod.getApplicationEndDate())
                .applicationPeriodText(source.getAplyYmd())
                .applicationMethod(source.getPlcyAplyMthdCn())
                .applicationUrl(source.getAplyUrlAddr())
                .referenceUrl(source.getRefUrlAddr1())
                .minAge(toInteger(source.getSprtTrgtMinAge()))
                .maxAge(toInteger(source.getSprtTrgtMaxAge()))
                .incomeConditionCode(source.getEarnCndSeCd())
                .minIncome(toLong(source.getEarnMinAmt()))
                .maxIncome(toPositiveLong(source.getEarnMaxAmt()))
                .incomeConditionText(source.getEarnEtcCn())
                .qualification(createQualification(
                        source.getAddAplyQlfcCndCn(),
                        source.getPtcpPrpTrgtCn()
                ))
                .syncedAt(LocalDateTime.now())
                .build();
    }

    private ApplicationPeriod parseApplicationPeriod(String applicationPeriodText) {
        if (isBlank(applicationPeriodText)) {
            return ApplicationPeriod.empty();
        }

        Matcher matcher = APPLICATION_PERIOD_PATTERN.matcher(applicationPeriodText.trim());
        if (!matcher.matches()) {
            return ApplicationPeriod.empty();
        }

        try {
            return new ApplicationPeriod(
                    LocalDate.parse(matcher.group(1), APPLICATION_DATE_FORMATTER),
                    LocalDate.parse(matcher.group(2), APPLICATION_DATE_FORMATTER)
            );
        } catch (DateTimeException exception) {
            return ApplicationPeriod.empty();
        }
    }

    private Integer toInteger(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long toLong(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long toPositiveLong(String value) {
        Long parsedValue = toLong(value);
        return parsedValue == null || parsedValue == 0 ? null : parsedValue;
    }

    private String createQualification(String additionalQualification, String participationTarget) {
        if (isBlank(additionalQualification) && isBlank(participationTarget)) {
            return null;
        }
        if (isBlank(additionalQualification)) {
            return participationTarget;
        }
        if (isBlank(participationTarget)) {
            return additionalQualification;
        }
        return additionalQualification + QUALIFICATION_SEPARATOR + participationTarget;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class ApplicationPeriod {

        private final LocalDate applicationStartDate;
        private final LocalDate applicationEndDate;

        private ApplicationPeriod(LocalDate applicationStartDate, LocalDate applicationEndDate) {
            this.applicationStartDate = applicationStartDate;
            this.applicationEndDate = applicationEndDate;
        }

        private static ApplicationPeriod empty() {
            return new ApplicationPeriod(null, null);
        }

        private LocalDate getApplicationStartDate() {
            return applicationStartDate;
        }

        private LocalDate getApplicationEndDate() {
            return applicationEndDate;
        }
    }
}
