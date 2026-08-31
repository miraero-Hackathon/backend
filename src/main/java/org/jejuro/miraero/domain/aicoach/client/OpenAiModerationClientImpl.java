package org.jejuro.miraero.domain.aicoach.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.client.dto.OpenAiModerationRequest;
import org.jejuro.miraero.domain.aicoach.client.dto.OpenAiModerationResponse;
import org.jejuro.miraero.global.config.OpenAiProperties;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OpenAiModerationClientImpl implements OpenAiModerationClient {

    private static final String MODERATIONS_PATH = "/moderations";

    private final OpenAiProperties openAiProperties;

    @Override
    public boolean isFlagged(String content) {
        if (!openAiProperties.isModerationEnabled() || !StringUtils.hasText(content)) {
            return false;
        }

        String requestUrl = UriComponentsBuilder.fromHttpUrl(openAiProperties.getBaseUrl())
                .path(MODERATIONS_PATH)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OpenAiModerationRequest> requestEntity = new HttpEntity<>(
                new OpenAiModerationRequest(content),
                headers
        );

        try {
            ResponseEntity<String> responseEntity = createRestTemplate().exchange(
                    requestUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            return extractFlagged(responseEntity.getBody());
        } catch (RestClientException | JsonProcessingException exception) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(openAiProperties.getTimeoutMs());
        requestFactory.setReadTimeout(openAiProperties.getTimeoutMs());
        return new RestTemplate(requestFactory);
    }

    private boolean extractFlagged(String responseBody) throws JsonProcessingException {
        if (!StringUtils.hasText(responseBody)) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        OpenAiModerationResponse response = new ObjectMapper().readValue(
                responseBody,
                OpenAiModerationResponse.class
        );
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        return response.getResults().stream().anyMatch(OpenAiModerationResponse.Result::isFlagged);
    }
}
