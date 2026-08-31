package org.jejuro.miraero.domain.product.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jejuro.miraero.domain.product.dto.external.FssDepositApiResponse;
import org.jejuro.miraero.domain.product.dto.external.FssSavingApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FssProductApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String auth;
    private final String depositPath;
    private final String savingPath;
    private final String topFinancialGroupNumber;
    private final Integer pageNumber;

    @Autowired
    public FssProductApiClient(
            @Value("${fss.api.base-url}") String baseUrl,
            @Value("${fss.api.auth}") String auth,
            @Value("${fss.api.deposit-path}") String depositPath,
            @Value("${fss.api.saving-path}") String savingPath,
            @Value("${fss.api.top-fin-grp-no}") String topFinancialGroupNumber,
            @Value("${fss.api.page-no}") Integer pageNumber
    ) {
        this(
                new RestTemplate(),
                new ObjectMapper(),
                baseUrl,
                auth,
                depositPath,
                savingPath,
                topFinancialGroupNumber,
                pageNumber
        );
    }

    FssProductApiClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            String baseUrl,
            String auth,
            String depositPath,
            String savingPath,
            String topFinancialGroupNumber,
            Integer pageNumber
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.auth = auth;
        this.depositPath = depositPath;
        this.savingPath = savingPath;
        this.topFinancialGroupNumber = topFinancialGroupNumber;
        this.pageNumber = pageNumber;
    }

    public FssDepositApiResponse getDepositProducts() {
        return request(depositPath, FssDepositApiResponse.class);
    }

    public FssSavingApiResponse getSavingProducts() {
        return request(savingPath, FssSavingApiResponse.class);
    }

    private <T> T request(String path, Class<T> responseType) {
        String requestUrl = createRequestUrl(path);

        try {
            String responseBody = restTemplate.getForObject(requestUrl, String.class);
            if (responseBody == null) {
                throw new RuntimeException("금융감독원 API 응답이 없습니다.");
            }

            T response = objectMapper.readValue(responseBody, responseType);
            if (response == null) {
                throw new RuntimeException("금융감독원 API 응답을 처리할 수 없습니다.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new RuntimeException("금융감독원 API 호출에 실패했습니다.", exception);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("금융감독원 API 응답 JSON 파싱에 실패했습니다.", exception);
        }
    }

    private String createRequestUrl(String path) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(path)
                .queryParam("auth", auth)
                .queryParam("topFinGrpNo", topFinancialGroupNumber)
                .queryParam("pageNo", pageNumber)
                .encode()
                .toUriString();
    }
}
