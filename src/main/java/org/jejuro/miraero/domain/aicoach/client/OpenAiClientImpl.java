package org.jejuro.miraero.domain.aicoach.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.client.dto.OpenAiResponsesRequest;
import org.jejuro.miraero.domain.aicoach.client.dto.OpenAiResponsesResponse;
import org.jejuro.miraero.global.config.OpenAiProperties;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class OpenAiClientImpl implements OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClientImpl.class);
    private static final String RESPONSES_PATH = "/responses";
    private static final String OUTPUT_TEXT_TYPE = "output_text";
    private static final String OUTPUT_TEXT_DELTA_TYPE = "response.output_text.delta";
    private static final String RESPONSE_FAILED_TYPE = "response.failed";
    private static final String RESPONSE_INCOMPLETE_TYPE = "response.incomplete";

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateText(String prompt) {
        return generateText(prompt, openAiProperties.getMaxOutputTokens());
    }

    @Override
    public String generateText(String prompt, int maxOutputTokens) {
        RestTemplate restTemplate = createRestTemplate();
        HttpEntity<OpenAiResponsesRequest> requestEntity = new HttpEntity<>(
                new OpenAiResponsesRequest(
                        openAiProperties.getModel(),
                        prompt,
                        openAiProperties.getReasoningEffort()
                ),
                createHeaders()
        );

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    getResponsesUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            return extractGeneratedText(responseEntity.getBody());
        } catch (RestClientException | JsonProcessingException exception) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public String generateTextStream(
            String prompt,
            int maxOutputTokens,
            Consumer<String> deltaConsumer
    ) {
        RestTemplate restTemplate = createRestTemplate();
        try {
            return restTemplate.execute(
                    getResponsesUrl(),
                    HttpMethod.POST,
                    request -> {
                        HttpHeaders headers = request.getHeaders();
                        headers.putAll(createHeaders());
                        headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                        objectMapper.writeValue(
                                request.getBody(),
                                new OpenAiResponsesRequest(
                                        openAiProperties.getModel(),
                                        prompt,
                                        openAiProperties.getReasoningEffort(),
                                        true
                                )
                        );
                    },
                    response -> extractStreamedText(response.getBody(), deltaConsumer)
            );
        } catch (RestClientException exception) {
            log.warn("OpenAI 스트리밍 API 호출 실패: {}", exception.getMessage());
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getResponsesUrl() {
        return UriComponentsBuilder.fromHttpUrl(openAiProperties.getBaseUrl())
                .path(RESPONSES_PATH)
                .toUriString();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(openAiProperties.getTimeoutMs());
        requestFactory.setReadTimeout(openAiProperties.getTimeoutMs());
        return new RestTemplate(requestFactory);
    }

    private String extractGeneratedText(String responseBody) throws JsonProcessingException {
        if (!StringUtils.hasText(responseBody)) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        OpenAiResponsesResponse response = objectMapper.readValue(
                responseBody,
                OpenAiResponsesResponse.class
        );
        if (response == null || response.getOutput() == null) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        String generatedText = response.getOutput().stream()
                .map(OpenAiResponsesResponse.Output::getContent)
                .filter(content -> content != null)
                .flatMap(List::stream)
                .filter(content -> OUTPUT_TEXT_TYPE.equals(content.getType()))
                .map(OpenAiResponsesResponse.OutputContent::getText)
                .filter(StringUtils::hasText)
                .reduce("", String::concat);

        if (!StringUtils.hasText(generatedText)) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        return generatedText;
    }

    private String extractStreamedText(
            InputStream inputStream,
            Consumer<String> deltaConsumer
    ) throws IOException {
        StringBuilder generatedText = new StringBuilder();
        String eventType = null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("event:")) {
                    eventType = line.substring("event:".length()).trim();
                    continue;
                }
                if (!line.startsWith("data:")) {
                    continue;
                }

                String data = line.substring("data:".length()).trim();
                if ("[DONE]".equals(data)) {
                    break;
                }
                processStreamEvent(eventType, data, generatedText, deltaConsumer);
            }
        }

        if (generatedText.isEmpty()) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        return generatedText.toString();
    }

    private void processStreamEvent(
            String eventType,
            String data,
            StringBuilder generatedText,
            Consumer<String> deltaConsumer
    ) throws JsonProcessingException {
        JsonNode event = objectMapper.readTree(data);
        String type = event.hasNonNull("type") ? event.get("type").asText() : eventType;
        if (OUTPUT_TEXT_DELTA_TYPE.equals(type) && event.hasNonNull("delta")) {
            String delta = event.get("delta").asText();
            if (StringUtils.hasText(delta)) {
                generatedText.append(delta);
                deltaConsumer.accept(delta);
            }
            return;
        }
        if (RESPONSE_FAILED_TYPE.equals(type) || RESPONSE_INCOMPLETE_TYPE.equals(type)) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }
}
