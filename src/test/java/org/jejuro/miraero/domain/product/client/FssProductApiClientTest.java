package org.jejuro.miraero.domain.product.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class FssProductApiClientTest {

    @Test
    void 정기예금_응답을_External_DTO로_반환한다() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        FssProductApiClient client = createClient(restTemplate);
        restTemplate.setResponseBody("""
            {
              "result": {
                "prdt_div": "D",
                "total_count": 0,
                "max_page_no": 1,
                "now_page_no": 1,
                "err_cd": "000",
                "err_msg": "정상",
                "baseList": [],
                "optionList": []
              }
            }
            """);

        assertEquals("D", client.getDepositProducts().getResult().getProductDivision());
        assertEquals(
                "https://fss.example.com/finlifeapi/depositProductsSearch.json"
                        + "?auth=test-auth&topFinGrpNo=020000&pageNo=1",
                restTemplate.getRequestUrl()
        );
    }

    @Test
    void 적금_응답을_External_DTO로_반환한다() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        FssProductApiClient client = createClient(restTemplate);
        restTemplate.setResponseBody("""
            {
              "result": {
                "prdt_div": "S",
                "total_count": 0,
                "max_page_no": 1,
                "now_page_no": 1,
                "err_cd": "000",
                "err_msg": "정상",
                "baseList": [],
                "optionList": []
              }
            }
            """);

        assertEquals("S", client.getSavingProducts().getResult().getProductDivision());
        assertEquals(
                "https://fss.example.com/finlifeapi/savingProductsSearch.json"
                        + "?auth=test-auth&topFinGrpNo=020000&pageNo=1",
                restTemplate.getRequestUrl()
        );
    }

    private FssProductApiClient createClient(RestTemplate restTemplate) {
        return new FssProductApiClient(
                restTemplate,
                new ObjectMapper(),
                "https://fss.example.com",
                "test-auth",
                "/finlifeapi/depositProductsSearch.json",
                "/finlifeapi/savingProductsSearch.json",
                "020000",
                1
        );
    }

    private static class StubRestTemplate extends RestTemplate {

        private String responseBody;
        private String requestUrl;

        void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        String getRequestUrl() {
            return requestUrl;
        }

        @Override
        public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
            requestUrl = url;
            return responseType.cast(responseBody);
        }
    }
}
