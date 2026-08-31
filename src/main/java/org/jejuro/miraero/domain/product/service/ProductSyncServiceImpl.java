package org.jejuro.miraero.domain.product.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jejuro.miraero.domain.product.client.FssProductApiClient;
import org.jejuro.miraero.domain.product.domain.DepositOption;
import org.jejuro.miraero.domain.product.domain.DepositProduct;
import org.jejuro.miraero.domain.product.domain.FinancialInstitution;
import org.jejuro.miraero.domain.product.domain.SavingOption;
import org.jejuro.miraero.domain.product.domain.SavingProduct;
import org.jejuro.miraero.domain.product.dto.external.FssDepositApiResponse;
import org.jejuro.miraero.domain.product.dto.external.FssDepositOption;
import org.jejuro.miraero.domain.product.dto.external.FssDepositProduct;
import org.jejuro.miraero.domain.product.dto.external.FssDepositResult;
import org.jejuro.miraero.domain.product.dto.external.FssSavingApiResponse;
import org.jejuro.miraero.domain.product.dto.external.FssSavingOption;
import org.jejuro.miraero.domain.product.dto.external.FssSavingProduct;
import org.jejuro.miraero.domain.product.dto.external.FssSavingResult;
import org.jejuro.miraero.domain.product.mapper.DepositOptionMapper;
import org.jejuro.miraero.domain.product.mapper.DepositProductMapper;
import org.jejuro.miraero.domain.product.mapper.FinancialInstitutionMapper;
import org.jejuro.miraero.domain.product.mapper.SavingOptionMapper;
import org.jejuro.miraero.domain.product.mapper.SavingProductMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductSyncServiceImpl implements ProductSyncService {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncServiceImpl.class);
    private static final String SUCCESS_ERROR_CODE = "000";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final FssProductApiClient fssProductApiClient;
    private final FinancialInstitutionMapper financialInstitutionMapper;
    private final DepositProductMapper depositProductMapper;
    private final DepositOptionMapper depositOptionMapper;
    private final SavingProductMapper savingProductMapper;
    private final SavingOptionMapper savingOptionMapper;
    private final String financialCompanyCode;

    @Autowired
    public ProductSyncServiceImpl(
            FssProductApiClient fssProductApiClient,
            FinancialInstitutionMapper financialInstitutionMapper,
            DepositProductMapper depositProductMapper,
            DepositOptionMapper depositOptionMapper,
            SavingProductMapper savingProductMapper,
            SavingOptionMapper savingOptionMapper,
            @Value("${fss.api.financial-company-code}") String financialCompanyCode
    ) {
        this.fssProductApiClient = fssProductApiClient;
        this.financialInstitutionMapper = financialInstitutionMapper;
        this.depositProductMapper = depositProductMapper;
        this.depositOptionMapper = depositOptionMapper;
        this.savingProductMapper = savingProductMapper;
        this.savingOptionMapper = savingOptionMapper;
        this.financialCompanyCode = financialCompanyCode;
    }

    @Override
    @Transactional
    public void syncDepositProducts() {
        log.info("예금상품 동기화를 시작합니다.");

        try {
            FssDepositResult result = getDepositResult();
            SyncCount count = syncDepositProducts(result);
            log.info("예금상품 동기화 완료 - 기관: {}, 상품: {}, 옵션: {}",
                    count.financialInstitutionCount, count.productCount, count.optionCount);
        } catch (RuntimeException exception) {
            log.error("예금상품 동기화 중 오류가 발생했습니다.", exception);
            throw exception;
        }
    }

    @Override
    @Transactional
    public void syncSavingProducts() {
        log.info("적금상품 동기화를 시작합니다.");

        try {
            FssSavingResult result = getSavingResult();
            SyncCount count = syncSavingProducts(result);
            log.info("적금상품 동기화 완료 - 기관: {}, 상품: {}, 옵션: {}",
                    count.financialInstitutionCount, count.productCount, count.optionCount);
        } catch (RuntimeException exception) {
            log.error("적금상품 동기화 중 오류가 발생했습니다.", exception);
            throw exception;
        }
    }

    @Override
    @Transactional
    public void syncAllProducts() {
        log.info("금융상품 전체 동기화를 시작합니다.");

        try {
            syncDepositProducts();
            syncSavingProducts();
            log.info("금융상품 전체 동기화를 완료했습니다.");
        } catch (RuntimeException exception) {
            log.error("금융상품 전체 동기화 중 오류가 발생했습니다.", exception);
            throw exception;
        }
    }

    private FssDepositResult getDepositResult() {
        FssDepositApiResponse response;

        try {
            response = fssProductApiClient.getDepositProducts();
        } catch (RuntimeException exception) {
            log.error("금감원 예금상품 API 호출에 실패했습니다.", exception);
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        if (response == null || response.getResult() == null
                || !SUCCESS_ERROR_CODE.equals(response.getResult().getErrorCode())) {
            log.error("금감원 예금상품 API가 정상 결과를 반환하지 않았습니다.");
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        return response.getResult();
    }

    private FssSavingResult getSavingResult() {
        FssSavingApiResponse response;

        try {
            response = fssProductApiClient.getSavingProducts();
        } catch (RuntimeException exception) {
            log.error("금감원 적금상품 API 호출에 실패했습니다.", exception);
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        if (response == null || response.getResult() == null
                || !SUCCESS_ERROR_CODE.equals(response.getResult().getErrorCode())) {
            log.error("금감원 적금상품 API가 정상 결과를 반환하지 않았습니다.");
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        return response.getResult();
    }

    private SyncCount syncDepositProducts(FssDepositResult result) {
        Map<String, List<FssDepositOption>> optionsByProductCode = groupOptionsByProductCode(
                emptyIfNull(result.getOptionList()), FssDepositOption::getFinancialProductCode);
        SyncCount count = new SyncCount();

        for (FssDepositProduct product : emptyIfNull(result.getBaseList())) {
            syncDepositProduct(product, optionsByProductCode, count);
        }
        return count;
    }

    private SyncCount syncSavingProducts(FssSavingResult result) {
        Map<String, List<FssSavingOption>> optionsByProductCode = groupOptionsByProductCode(
                emptyIfNull(result.getOptionList()), FssSavingOption::getFinancialProductCode);
        SyncCount count = new SyncCount();

        for (FssSavingProduct product : emptyIfNull(result.getBaseList())) {
            syncSavingProduct(product, optionsByProductCode, count);
        }
        return count;
    }

    private void syncDepositProduct(
            FssDepositProduct source,
            Map<String, List<FssDepositOption>> optionsByProductCode,
            SyncCount count
    ) {
        if (!isTargetFinancialCompany(source)) {
            return;
        }

        if (!hasRequiredDepositProductFields(source)) {
            log.warn("필수값이 없는 예금상품 데이터를 건너뜁니다.");
            return;
        }

        try {
            FinancialInstitution financialInstitution = upsertFinancialInstitution(
                    source.getFinancialCompanyCode(), source.getFinancialCompanyName());
            count.financialInstitutionCount++;

            DepositProduct depositProduct = createDepositProduct(source, financialInstitution.getFinancialInstitutionId());
            depositProductMapper.upsert(depositProduct);
            requireId(depositProduct.getDepositProductId(), "예금상품");
            count.productCount++;

            for (FssDepositOption option : optionsByProductCode.getOrDefault(
                    source.getFinancialProductCode(), List.of())) {
                if (!isTargetFinancialCompany(option)) {
                    continue;
                }
                if (upsertDepositOption(option, depositProduct.getDepositProductId())) {
                    count.optionCount++;
                }
            }
        } catch (DateTimeException exception) {
            log.warn("예금상품 데이터 처리 중 오류가 발생해 해당 상품을 건너뜁니다. productCode={}",
                    source.getFinancialProductCode(), exception);
        }
    }

    private void syncSavingProduct(
            FssSavingProduct source,
            Map<String, List<FssSavingOption>> optionsByProductCode,
            SyncCount count
    ) {
        if (!isTargetFinancialCompany(source)) {
            return;
        }

        if (!hasRequiredSavingProductFields(source)) {
            log.warn("필수값이 없는 적금상품 데이터를 건너뜁니다.");
            return;
        }

        try {
            FinancialInstitution financialInstitution = upsertFinancialInstitution(
                    source.getFinancialCompanyCode(), source.getFinancialCompanyName());
            count.financialInstitutionCount++;

            SavingProduct savingProduct = createSavingProduct(source, financialInstitution.getFinancialInstitutionId());
            savingProductMapper.upsert(savingProduct);
            requireId(savingProduct.getSavingProductId(), "적금상품");
            count.productCount++;

            for (FssSavingOption option : optionsByProductCode.getOrDefault(
                    source.getFinancialProductCode(), List.of())) {
                if (!isTargetFinancialCompany(option)) {
                    continue;
                }
                if (upsertSavingOption(option, savingProduct.getSavingProductId())) {
                    count.optionCount++;
                }
            }
        } catch (DateTimeException exception) {
            log.warn("적금상품 데이터 처리 중 오류가 발생해 해당 상품을 건너뜁니다. productCode={}",
                    source.getFinancialProductCode(), exception);
        }
    }

    private FinancialInstitution upsertFinancialInstitution(String code, String name) {
        FinancialInstitution financialInstitution = FinancialInstitution.builder()
                .financialInstitutionCode(code)
                .financialInstitutionName(name)
                .build();
        financialInstitutionMapper.upsert(financialInstitution);
        requireId(financialInstitution.getFinancialInstitutionId(), "금융기관");
        return financialInstitution;
    }

    private DepositProduct createDepositProduct(FssDepositProduct source, Long financialInstitutionId) {
        return DepositProduct.builder()
                .financialInstitutionId(financialInstitutionId)
                .productCode(source.getFinancialProductCode())
                .productName(source.getFinancialProductName())
                .joinMethod(source.getJoinWay())
                .joinTarget(source.getJoinMember())
                .joinRestriction(source.getJoinDeny())
                .specialCondition(source.getSpecialCondition())
                .maturityInterest(source.getMaturityInterest())
                .maxLimit(source.getMaxLimit())
                .notice(source.getEtcNote())
                .disclosureMonth(source.getDisclosureMonth())
                .disclosureStartDate(parseDate(source.getDisclosureStartDay()))
                .disclosureEndDate(parseDate(source.getDisclosureEndDay()))
                .submittedAt(parseDateTime(source.getFinancialCompanySubmittedDay()))
                .build();
    }

    private SavingProduct createSavingProduct(FssSavingProduct source, Long financialInstitutionId) {
        return SavingProduct.builder()
                .financialInstitutionId(financialInstitutionId)
                .productCode(source.getFinancialProductCode())
                .productName(source.getFinancialProductName())
                .joinMethod(source.getJoinWay())
                .joinTarget(source.getJoinMember())
                .joinRestriction(source.getJoinDeny())
                .specialCondition(source.getSpecialCondition())
                .maturityInterest(source.getMaturityInterest())
                .maxLimit(source.getMaxLimit())
                .notice(source.getEtcNote())
                .disclosureMonth(source.getDisclosureMonth())
                .disclosureStartDate(parseDate(source.getDisclosureStartDay()))
                .disclosureEndDate(parseDate(source.getDisclosureEndDay()))
                .submittedAt(parseDateTime(source.getFinancialCompanySubmittedDay()))
                .build();
    }

    private boolean upsertDepositOption(FssDepositOption source, Long depositProductId) {
        if (source == null || isBlank(source.getInterestRateType()) || isBlank(source.getSaveTerm())) {
            log.warn("필수값이 없는 예금상품 옵션 데이터를 건너뜁니다.");
            return false;
        }

        try {
            DepositOption depositOption = DepositOption.builder()
                    .depositProductId(depositProductId)
                    .interestRateType(source.getInterestRateType())
                    .saveTerm(Integer.valueOf(source.getSaveTerm()))
                    .baseInterestRate(source.getInterestRate())
                    .maxInterestRate(source.getMaximumInterestRate())
                    .build();
            depositOptionMapper.upsert(depositOption);
            return true;
        } catch (NumberFormatException exception) {
            log.warn("저축 기간 형식이 올바르지 않은 예금상품 옵션을 건너뜁니다. saveTerm={}", source.getSaveTerm());
            return false;
        }
    }

    private boolean upsertSavingOption(FssSavingOption source, Long savingProductId) {
        if (source == null || isBlank(source.getInterestRateType())
                || isBlank(source.getReserveType()) || isBlank(source.getSaveTerm())) {
            log.warn("필수값이 없는 적금상품 옵션 데이터를 건너뜁니다.");
            return false;
        }

        try {
            SavingOption savingOption = SavingOption.builder()
                    .savingProductId(savingProductId)
                    .interestRateType(source.getInterestRateType())
                    .reserveType(source.getReserveType())
                    .saveTerm(Integer.valueOf(source.getSaveTerm()))
                    .baseInterestRate(source.getInterestRate())
                    .maxInterestRate(source.getMaximumInterestRate())
                    .build();
            savingOptionMapper.upsert(savingOption);
            return true;
        } catch (NumberFormatException exception) {
            log.warn("저축 기간 형식이 올바르지 않은 적금상품 옵션을 건너뜁니다. saveTerm={}", source.getSaveTerm());
            return false;
        }
    }

    private <T> Map<String, List<T>> groupOptionsByProductCode(
            List<T> options,
            Function<T, String> productCodeExtractor
    ) {
        Map<String, List<T>> optionsByProductCode = new HashMap<>();

        for (T option : options) {
            if (option == null || isBlank(productCodeExtractor.apply(option))) {
                log.warn("상품 코드가 없는 옵션 데이터를 건너뜁니다.");
                continue;
            }
            optionsByProductCode.computeIfAbsent(productCodeExtractor.apply(option), ignored -> new ArrayList<>())
                    .add(option);
        }
        return optionsByProductCode;
    }

    private <T> List<T> emptyIfNull(List<T> values) {
        return values == null ? List.of() : values;
    }

    private boolean hasRequiredDepositProductFields(FssDepositProduct source) {
        return source != null
                && !isBlank(source.getFinancialCompanyCode())
                && !isBlank(source.getFinancialCompanyName())
                && !isBlank(source.getFinancialProductCode())
                && !isBlank(source.getFinancialProductName())
                && !isBlank(source.getDisclosureMonth());
    }

    private boolean isTargetFinancialCompany(FssDepositProduct source) {
        return source != null
                && financialCompanyCode.equals(source.getFinancialCompanyCode())
                && isFssProductCode(source.getFinancialProductCode());
    }

    private boolean isTargetFinancialCompany(FssSavingProduct source) {
        return source != null
                && financialCompanyCode.equals(source.getFinancialCompanyCode())
                && isFssProductCode(source.getFinancialProductCode());
    }

    private boolean isTargetFinancialCompany(FssDepositOption source) {
        return source != null && financialCompanyCode.equals(source.getFinancialCompanyCode());
    }

    private boolean isTargetFinancialCompany(FssSavingOption source) {
        return source != null && financialCompanyCode.equals(source.getFinancialCompanyCode());
    }

    private boolean hasRequiredSavingProductFields(FssSavingProduct source) {
        return source != null
                && !isBlank(source.getFinancialCompanyCode())
                && !isBlank(source.getFinancialCompanyName())
                && !isBlank(source.getFinancialProductCode())
                && !isBlank(source.getFinancialProductName())
                && !isBlank(source.getDisclosureMonth());
    }

    private boolean isFssProductCode(String productCode) {
        if (isBlank(productCode)) {
            return false;
        }

        for (int index = 0; index < productCode.length(); index++) {
            char character = productCode.charAt(index);
            if (character < '0' || character > '9') {
                return false;
            }
        }
        return true;
    }

    private LocalDate parseDate(String value) {
        return isBlank(value) ? null : LocalDate.parse(value, DATE_FORMATTER);
    }

    private LocalDateTime parseDateTime(String value) {
        return isBlank(value) ? null : LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    private void requireId(Long id, String target) {
        if (id == null) {
            throw new IllegalStateException(target + " upsert 후 PK를 확인할 수 없습니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class SyncCount {

        private int financialInstitutionCount;
        private int productCount;
        private int optionCount;
    }
}
