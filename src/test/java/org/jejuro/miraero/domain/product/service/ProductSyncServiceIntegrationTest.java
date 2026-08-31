package org.jejuro.miraero.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jejuro.miraero.domain.product.client.FssProductApiClient;
import org.jejuro.miraero.domain.product.dto.external.FssDepositApiResponse;
import org.jejuro.miraero.domain.product.dto.external.FssDepositOption;
import org.jejuro.miraero.domain.product.dto.external.FssDepositProduct;
import org.jejuro.miraero.domain.product.dto.external.FssSavingApiResponse;
import org.jejuro.miraero.domain.product.dto.external.FssSavingOption;
import org.jejuro.miraero.domain.product.dto.external.FssSavingProduct;
import org.jejuro.miraero.global.config.DataSourceConfig;
import org.jejuro.miraero.global.config.MyBatisConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** 실제 금감원 Open API와 Docker MySQL에 데이터를 남기는 금융상품 동기화 통합 테스트. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProductSyncServiceIntegrationTest.ProductSyncIntegrationConfig.class)
class ProductSyncServiceIntegrationTest {

    @Autowired
    private FssProductApiClient fssProductApiClient;

    @Autowired
    private ProductSyncService productSyncService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${fss.api.financial-company-code}")
    private String financialCompanyCode;

    @Test
    void shouldSyncAllProductsToDockerMySqlAndMatchRepresentativeApiData() {
        FssDepositApiResponse depositResponse = fssProductApiClient.getDepositProducts();
        FssSavingApiResponse savingResponse = fssProductApiClient.getSavingProducts();
        FssDepositProduct depositProduct = selectDepositProductWithOption(depositResponse);
        FssDepositOption depositOption = selectDepositOption(depositResponse, depositProduct);
        FssSavingProduct savingProduct = selectSavingProductWithOption(savingResponse);
        FssSavingOption savingOption = selectSavingOption(savingResponse, savingProduct);
        Map<String, Long> beforeCounts = loadTableCounts();

        productSyncService.syncAllProducts();

        Map<String, Long> afterCounts = loadTableCounts();
        assertAllProductTablesContainData(afterCounts);
        assertRepresentativeDepositData(depositProduct, depositOption);
        assertRepresentativeSavingData(savingProduct, savingOption);
        assertForeignKeyIntegrity();
        assertCountsAreNonDecreasing(beforeCounts, afterCounts);
    }

    @Test
    void shouldNotCreateDuplicatesWhenSyncRunsTwice() {
        productSyncService.syncAllProducts();
        Map<String, Long> firstSyncCounts = loadTableCounts();

        productSyncService.syncAllProducts();
        Map<String, Long> secondSyncCounts = loadTableCounts();

        assertAllProductTablesContainData(firstSyncCounts);
        assertAllProductTablesContainData(secondSyncCounts);
        assertNoDuplicateUniqueKeys();
    }

    @Test
    void shouldMaintainForeignKeyIntegrityAfterProductSync() {
        productSyncService.syncAllProducts();

        assertForeignKeyIntegrity();
    }

    private Map<String, Long> loadTableCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("financial_institution", count("SELECT COUNT(*) FROM financial_institution"));
        counts.put("deposit_product", count("SELECT COUNT(*) FROM deposit_product"));
        counts.put("deposit_option", count("SELECT COUNT(*) FROM deposit_option"));
        counts.put("saving_product", count("SELECT COUNT(*) FROM saving_product"));
        counts.put("saving_option", count("SELECT COUNT(*) FROM saving_option"));
        return counts;
    }

    private void assertAllProductTablesContainData(Map<String, Long> counts) {
        counts.forEach((tableName, rowCount) -> assertTrue(rowCount > 0,
                () -> tableName + " 테이블에 동기화 데이터가 없습니다."));
    }

    private void assertCountsAreNonDecreasing(Map<String, Long> beforeCounts, Map<String, Long> afterCounts) {
        afterCounts.forEach((tableName, afterCount) -> assertTrue(afterCount >= beforeCounts.get(tableName),
                () -> tableName + " 행 수가 동기화 후 감소했습니다."));
    }

    private void assertRepresentativeDepositData(FssDepositProduct product, FssDepositOption option) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT fi.financial_institution_code, fi.financial_institution_name,
                       dp.deposit_product_id, dp.product_code, dp.product_name, dp.disclosure_month,
                       dp.join_method, dp.join_target, dp.join_restriction, dp.max_limit
                FROM deposit_product dp
                JOIN financial_institution fi ON fi.financial_institution_id = dp.financial_institution_id
                WHERE fi.financial_institution_code = ? AND dp.product_code = ?
                """, product.getFinancialCompanyCode(), product.getFinancialProductCode());

        assertEquals(product.getFinancialCompanyCode(), row.get("financial_institution_code"));
        assertEquals(product.getFinancialCompanyName(), row.get("financial_institution_name"));
        assertEquals(product.getFinancialProductCode(), row.get("product_code"));
        assertEquals(product.getFinancialProductName(), row.get("product_name"));
        assertEquals(product.getDisclosureMonth(), row.get("disclosure_month"));
        assertEquals(product.getJoinWay(), row.get("join_method"));
        assertEquals(product.getJoinMember(), row.get("join_target"));
        assertEquals(product.getJoinDeny(), row.get("join_restriction"));
        assertEquals(product.getMaxLimit(), toLong(row.get("max_limit")));

        Map<String, Object> optionRow = jdbcTemplate.queryForMap("""
                SELECT interest_rate_type, save_term, base_interest_rate, max_interest_rate
                FROM deposit_option
                WHERE deposit_product_id = ? AND interest_rate_type = ? AND save_term = ?
                """, row.get("deposit_product_id"), option.getInterestRateType(), Integer.valueOf(option.getSaveTerm()));

        assertEquals(option.getInterestRateType(), optionRow.get("interest_rate_type"));
        assertEquals(Integer.valueOf(option.getSaveTerm()), toInteger(optionRow.get("save_term")));
        assertBigDecimalEquals(option.getInterestRate(), optionRow.get("base_interest_rate"));
        assertBigDecimalEquals(option.getMaximumInterestRate(), optionRow.get("max_interest_rate"));
    }

    private void assertRepresentativeSavingData(FssSavingProduct product, FssSavingOption option) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT fi.financial_institution_code, fi.financial_institution_name,
                       sp.saving_product_id, sp.product_code, sp.product_name, sp.disclosure_month,
                       sp.join_method, sp.join_target, sp.join_restriction, sp.max_limit
                FROM saving_product sp
                JOIN financial_institution fi ON fi.financial_institution_id = sp.financial_institution_id
                WHERE fi.financial_institution_code = ? AND sp.product_code = ?
                """, product.getFinancialCompanyCode(), product.getFinancialProductCode());

        assertEquals(product.getFinancialCompanyCode(), row.get("financial_institution_code"));
        assertEquals(product.getFinancialCompanyName(), row.get("financial_institution_name"));
        assertEquals(product.getFinancialProductCode(), row.get("product_code"));
        assertEquals(product.getFinancialProductName(), row.get("product_name"));
        assertEquals(product.getDisclosureMonth(), row.get("disclosure_month"));
        assertEquals(product.getJoinWay(), row.get("join_method"));
        assertEquals(product.getJoinMember(), row.get("join_target"));
        assertEquals(product.getJoinDeny(), row.get("join_restriction"));
        assertEquals(product.getMaxLimit(), toLong(row.get("max_limit")));

        Map<String, Object> optionRow = jdbcTemplate.queryForMap("""
                SELECT interest_rate_type, reserve_type, save_term, base_interest_rate, max_interest_rate
                FROM saving_option
                WHERE saving_product_id = ? AND interest_rate_type = ?
                  AND reserve_type = ? AND save_term = ?
                """, row.get("saving_product_id"), option.getInterestRateType(), option.getReserveType(),
                Integer.valueOf(option.getSaveTerm()));

        assertEquals(option.getInterestRateType(), optionRow.get("interest_rate_type"));
        assertEquals(option.getReserveType(), optionRow.get("reserve_type"));
        assertEquals(Integer.valueOf(option.getSaveTerm()), toInteger(optionRow.get("save_term")));
        assertBigDecimalEquals(option.getInterestRate(), optionRow.get("base_interest_rate"));
        assertBigDecimalEquals(option.getMaximumInterestRate(), optionRow.get("max_interest_rate"));
    }

    private void assertNoDuplicateUniqueKeys() {
        assertEquals(0, count("""
                SELECT COUNT(*) FROM (
                    SELECT financial_institution_code
                    FROM financial_institution
                    GROUP BY financial_institution_code
                    HAVING COUNT(*) > 1
                ) duplicates
                """));
        assertEquals(0, count("""
                SELECT COUNT(*) FROM (
                    SELECT financial_institution_id, product_code
                    FROM deposit_product
                    GROUP BY financial_institution_id, product_code
                    HAVING COUNT(*) > 1
                ) duplicates
                """));
        assertEquals(0, count("""
                SELECT COUNT(*) FROM (
                    SELECT financial_institution_id, product_code
                    FROM saving_product
                    GROUP BY financial_institution_id, product_code
                    HAVING COUNT(*) > 1
                ) duplicates
                """));
        assertEquals(0, count("""
                SELECT COUNT(*) FROM (
                    SELECT deposit_product_id, interest_rate_type, save_term
                    FROM deposit_option
                    GROUP BY deposit_product_id, interest_rate_type, save_term
                    HAVING COUNT(*) > 1
                ) duplicates
                """));
        assertEquals(0, count("""
                SELECT COUNT(*) FROM (
                    SELECT saving_product_id, interest_rate_type, reserve_type, save_term
                    FROM saving_option
                    GROUP BY saving_product_id, interest_rate_type, reserve_type, save_term
                    HAVING COUNT(*) > 1
                ) duplicates
                """));
    }

    private void assertForeignKeyIntegrity() {
        assertEquals(0, count("""
                SELECT COUNT(*) FROM deposit_product dp
                LEFT JOIN financial_institution fi ON fi.financial_institution_id = dp.financial_institution_id
                WHERE fi.financial_institution_id IS NULL
                """));
        assertEquals(0, count("""
                SELECT COUNT(*) FROM saving_product sp
                LEFT JOIN financial_institution fi ON fi.financial_institution_id = sp.financial_institution_id
                WHERE fi.financial_institution_id IS NULL
                """));
        assertEquals(0, count("""
                SELECT COUNT(*) FROM deposit_option doo
                LEFT JOIN deposit_product dp ON dp.deposit_product_id = doo.deposit_product_id
                WHERE dp.deposit_product_id IS NULL
                """));
        assertEquals(0, count("""
                SELECT COUNT(*) FROM saving_option so
                LEFT JOIN saving_product sp ON sp.saving_product_id = so.saving_product_id
                WHERE sp.saving_product_id IS NULL
                """));
    }

    private FssDepositProduct selectDepositProductWithOption(FssDepositApiResponse response) {
        assertNotNull(response);
        assertNotNull(response.getResult());
        return response.getResult().getBaseList().stream()
                .filter(product -> financialCompanyCode.equals(product.getFinancialCompanyCode()))
                .filter(product -> isFssProductCode(product.getFinancialProductCode()))
                .filter(this::hasRequiredDepositProductFields)
                .filter(product -> response.getResult().getOptionList().stream()
                        .anyMatch(option -> isMatchingDepositOption(option, product)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("대표 예금상품과 옵션을 찾을 수 없습니다."));
    }

    private FssDepositOption selectDepositOption(FssDepositApiResponse response, FssDepositProduct product) {
        return response.getResult().getOptionList().stream()
                .filter(option -> isMatchingDepositOption(option, product))
                .findFirst()
                .orElseThrow(() -> new AssertionError("대표 예금상품 옵션을 찾을 수 없습니다."));
    }

    private FssSavingProduct selectSavingProductWithOption(FssSavingApiResponse response) {
        assertNotNull(response);
        assertNotNull(response.getResult());
        return response.getResult().getBaseList().stream()
                .filter(product -> financialCompanyCode.equals(product.getFinancialCompanyCode()))
                .filter(product -> isFssProductCode(product.getFinancialProductCode()))
                .filter(this::hasRequiredSavingProductFields)
                .filter(product -> response.getResult().getOptionList().stream()
                        .anyMatch(option -> isMatchingSavingOption(option, product)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("대표 적금상품과 옵션을 찾을 수 없습니다."));
    }

    private FssSavingOption selectSavingOption(FssSavingApiResponse response, FssSavingProduct product) {
        return response.getResult().getOptionList().stream()
                .filter(option -> isMatchingSavingOption(option, product))
                .findFirst()
                .orElseThrow(() -> new AssertionError("대표 적금상품 옵션을 찾을 수 없습니다."));
    }

    private boolean hasRequiredDepositProductFields(FssDepositProduct product) {
        return product != null && hasRequiredProductFields(product.getFinancialCompanyCode(),
                product.getFinancialProductCode(), product.getFinancialProductName(), product.getDisclosureMonth());
    }

    private boolean hasRequiredSavingProductFields(FssSavingProduct product) {
        return product != null && hasRequiredProductFields(product.getFinancialCompanyCode(),
                product.getFinancialProductCode(), product.getFinancialProductName(), product.getDisclosureMonth());
    }

    private boolean hasRequiredProductFields(String companyCode, String productCode, String productName, String disclosureMonth) {
        return !isBlank(companyCode) && !isBlank(productCode) && !isBlank(productName) && !isBlank(disclosureMonth);
    }

    private boolean isMatchingDepositOption(FssDepositOption option, FssDepositProduct product) {
        return option != null && product.getFinancialProductCode().equals(option.getFinancialProductCode())
                && !isBlank(option.getInterestRateType()) && isInteger(option.getSaveTerm());
    }

    private boolean isMatchingSavingOption(FssSavingOption option, FssSavingProduct product) {
        return option != null && product.getFinancialProductCode().equals(option.getFinancialProductCode())
                && !isBlank(option.getInterestRateType()) && !isBlank(option.getReserveType())
                && isInteger(option.getSaveTerm());
    }

    private long count(String sql) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        assertNotNull(count);
        return count;
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private void assertBigDecimalEquals(BigDecimal expected, Object actual) {
        if (expected == null) {
            assertEquals(null, actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(0, expected.compareTo((BigDecimal) actual));
    }

    private boolean isInteger(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

    @Configuration
    @Import({DataSourceConfig.class, MyBatisConfig.class, FssProductApiClient.class, ProductSyncServiceImpl.class})
    static class ProductSyncIntegrationConfig {

        @org.springframework.context.annotation.Bean
        JdbcTemplate jdbcTemplate(javax.sql.DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }
}
