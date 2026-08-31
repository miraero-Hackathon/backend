package org.jejuro.miraero.domain.youthpolicy.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

class YouthPolicyApiClientTest {

    @Test
    void getYouthPolicies_deserializesResponseAndPassesPageNumber() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        YouthPolicyApiClient client = createClient(restTemplate);
        restTemplate.setResponseBody("""
                {"resultCode":200,"resultMessage":"성공","result":{"pagging":{"totCount":1,"pageNum":2,"pageSize":10},"youthPolicyList":[{"plcyNo":"20260806005400213322","plcyNm":"청년 정책","sprtTrgtMinAge":"19","earnMaxAmt":"0"}]}}
                """);

        assertEquals(200, client.getYouthPolicies(2).getResultCode());
        assertEquals(1, client.getYouthPolicies(2).getResult().getPagging().getTotCount());
        assertEquals("20260806005400213322", client.getYouthPolicies(2)
                .getResult().getYouthPolicyList().get(0).getPlcyNo());
        assertEquals(
                "https://youth.example.com/go/ythip/getPlcy"
                        + "?apiKeyNm=test-key&pageNum=2&rtnType=json",
                restTemplate.getRequestUrl()
        );
        assertEquals("application/json", restTemplate.getRequestHeaders().getFirst(HttpHeaders.ACCEPT));
    }

    @Test
    void getYouthPolicies_throwsExceptionWhenResultCodeIsNotSuccessful() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        YouthPolicyApiClient client = createClient(restTemplate);
        restTemplate.setResponseBody("{\"resultCode\":500,\"resultMessage\":\"실패\"}");

        assertThrows(RuntimeException.class, () -> client.getYouthPolicies(1));
    }

    @Test
    void getYouthPolicies_passesPolicyKeyword() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        YouthPolicyApiClient client = createClient(restTemplate);
        restTemplate.setResponseBody("{\"resultCode\":200,\"result\":{\"pagging\":{\"totCount\":0,\"pageNum\":1,\"pageSize\":10},\"youthPolicyList\":[]}}");

        client.getYouthPolicies(1, "주거지원");

        assertEquals(
                "https://youth.example.com/go/ythip/getPlcy?apiKeyNm=test-key&pageNum=1&rtnType=json&plcyKywdNm=%EC%A3%BC%EA%B1%B0%EC%A7%80%EC%9B%90",
                restTemplate.getRequestUrl()
        );
    }

    @Test
    void getYouthPolicies_deserializesXmlResponse() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        YouthPolicyApiClient client = createClient(restTemplate);
        restTemplate.setResponseBody("""
                <response><resultCode>200</resultCode><result><pagging><totCount>0</totCount><pageNum>1</pageNum><pageSize>10</pageSize></pagging></result></response>
                """);

        assertEquals(0, client.getYouthPolicies(1).getResult().getPagging().getTotCount());
    }

    @Test
    void getYouthPolicies_retriesTemporaryApiFailure() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        YouthPolicyApiClient client = createClient(restTemplate);
        restTemplate.setFailuresBeforeSuccess(1);
        restTemplate.setResponseBody("{\"resultCode\":200,\"result\":{\"pagging\":{\"totCount\":0,\"pageNum\":1,\"pageSize\":10},\"youthPolicyList\":[]}}");

        assertEquals(200, client.getYouthPolicies(1).getResultCode());
        assertEquals(2, restTemplate.getRequestCount());
    }

    private YouthPolicyApiClient createClient(RestTemplate restTemplate) {
        return new YouthPolicyApiClient(
                restTemplate,
                new ObjectMapper(),
                "https://youth.example.com",
                "/go/ythip/getPlcy",
                "test-key",
                10
        );
    }

    private static class StubRestTemplate extends RestTemplate {

        private String responseBody;
        private String requestUrl;
        private HttpHeaders requestHeaders;
        private int failuresBeforeSuccess;
        private int requestCount;

        void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        void setFailuresBeforeSuccess(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
        }

        String getRequestUrl() {
            return requestUrl;
        }

        @Override
        public <T> ResponseEntity<T> exchange(
                java.net.URI url,
                HttpMethod method,
                HttpEntity<?> requestEntity,
                Class<T> responseType
        ) {
            requestCount++;
            if (failuresBeforeSuccess > 0) {
                failuresBeforeSuccess--;
                throw new ResourceAccessException("temporary failure");
            }
            requestUrl = url.toString();
            requestHeaders = requestEntity.getHeaders();
            return ResponseEntity.ok(responseType.cast(responseBody));
        }

        HttpHeaders getRequestHeaders() {
            return requestHeaders;
        }

        int getRequestCount() {
            return requestCount;
        }
    }
}
