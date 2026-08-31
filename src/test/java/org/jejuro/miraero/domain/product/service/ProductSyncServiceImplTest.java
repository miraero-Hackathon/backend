package org.jejuro.miraero.domain.product.service;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jejuro.miraero.domain.product.client.FssProductApiClient;
import org.jejuro.miraero.domain.product.dto.external.FssDepositApiResponse;
import org.jejuro.miraero.domain.product.dto.external.FssSavingApiResponse;
import org.jejuro.miraero.domain.product.mapper.DepositOptionMapper;
import org.jejuro.miraero.domain.product.mapper.DepositProductMapper;
import org.jejuro.miraero.domain.product.mapper.FinancialInstitutionMapper;
import org.jejuro.miraero.domain.product.mapper.SavingOptionMapper;
import org.jejuro.miraero.domain.product.mapper.SavingProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductSyncServiceImplTest {

    private static final String KB_FINANCIAL_COMPANY_CODE = "0010927";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FssProductApiClient fssProductApiClient;

    @Mock
    private FinancialInstitutionMapper financialInstitutionMapper;

    @Mock
    private DepositProductMapper depositProductMapper;

    @Mock
    private DepositOptionMapper depositOptionMapper;

    @Mock
    private SavingProductMapper savingProductMapper;

    @Mock
    private SavingOptionMapper savingOptionMapper;

    @Test
    void syncDepositProducts_skipsManualAndOtherFinancialCompanyProducts() throws Exception {
        when(fssProductApiClient.getDepositProducts()).thenReturn(readDepositResponse());

        createService().syncDepositProducts();

        verifyNoInteractions(
                financialInstitutionMapper,
                depositProductMapper,
                depositOptionMapper,
                savingProductMapper,
                savingOptionMapper
        );
    }

    @Test
    void syncSavingProducts_skipsManualAndOtherFinancialCompanyProducts() throws Exception {
        when(fssProductApiClient.getSavingProducts()).thenReturn(readSavingResponse());

        createService().syncSavingProducts();

        verifyNoInteractions(
                financialInstitutionMapper,
                depositProductMapper,
                depositOptionMapper,
                savingProductMapper,
                savingOptionMapper
        );
    }

    private ProductSyncService createService() {
        return new ProductSyncServiceImpl(
                fssProductApiClient,
                financialInstitutionMapper,
                depositProductMapper,
                depositOptionMapper,
                savingProductMapper,
                savingOptionMapper,
                KB_FINANCIAL_COMPANY_CODE
        );
    }

    private FssDepositApiResponse readDepositResponse() throws Exception {
        return objectMapper.readValue("""
                {
                  "result": {
                    "err_cd": "000",
                    "baseList": [
                      {"fin_co_no": "0010927", "fin_prdt_cd": "DP_MANUAL"},
                      {"fin_co_no": "9999999", "fin_prdt_cd": "123456"}
                    ]
                  }
                }
                """, FssDepositApiResponse.class);
    }

    private FssSavingApiResponse readSavingResponse() throws Exception {
        return objectMapper.readValue("""
                {
                  "result": {
                    "err_cd": "000",
                    "baseList": [
                      {"fin_co_no": "0010927", "fin_prdt_cd": "DP_MANUAL"},
                      {"fin_co_no": "9999999", "fin_prdt_cd": "123456"}
                    ]
                  }
                }
                """, FssSavingApiResponse.class);
    }
}
