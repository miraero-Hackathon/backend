package org.jejuro.miraero.domain.product.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jejuro.miraero.domain.product.dto.external.FssDepositApiResponse;
import org.jejuro.miraero.domain.product.dto.external.FssSavingApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;

/** 실제 금감원 Open API와 application.properties의 인증 설정을 사용하는 통합 테스트. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FssProductApiClientIntegrationTest.ApiClientIntegrationConfig.class)
class FssProductApiClientIntegrationTest {

    private static final String SUCCESS_ERROR_CODE = "000";
    private static final Logger log = LoggerFactory.getLogger(FssProductApiClientIntegrationTest.class);

    @Autowired
    private FssProductApiClient fssProductApiClient;

    @Test
    void shouldCallDepositApiSuccessfully() {
        FssDepositApiResponse response = fssProductApiClient.getDepositProducts();

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertEquals(SUCCESS_ERROR_CODE, response.getResult().getErrorCode());
        assertNotNull(response.getResult().getBaseList());
        assertFalse(response.getResult().getBaseList().isEmpty());
        assertNotNull(response.getResult().getOptionList());
        assertFalse(response.getResult().getOptionList().isEmpty());
        log.info("실제 예금 API 응답 검증 완료 - baseList: {}, optionList: {}",
                response.getResult().getBaseList().size(), response.getResult().getOptionList().size());
    }

    @Test
    void shouldCallSavingApiSuccessfully() {
        FssSavingApiResponse response = fssProductApiClient.getSavingProducts();

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertEquals(SUCCESS_ERROR_CODE, response.getResult().getErrorCode());
        assertNotNull(response.getResult().getBaseList());
        assertFalse(response.getResult().getBaseList().isEmpty());
        assertNotNull(response.getResult().getOptionList());
        assertFalse(response.getResult().getOptionList().isEmpty());
        log.info("실제 적금 API 응답 검증 완료 - baseList: {}, optionList: {}",
                response.getResult().getBaseList().size(), response.getResult().getOptionList().size());
    }

    @Configuration
    @PropertySource("classpath:application.properties")
    @Import(FssProductApiClient.class)
    static class ApiClientIntegrationConfig {
    }
}
