package org.jejuro.miraero.domain.youthpolicy.client;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiItem;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiResponse;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiPaging;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiResult;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class YouthPolicyApiClient {

    private static final int SUCCESS_RESULT_CODE = 200;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MILLIS = 1_000L;
    private static final String USER_AGENT = "MiraeroYouthPolicyClient/1.0";
    private static final Logger log = LoggerFactory.getLogger(YouthPolicyApiClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String policyPath;
    private final String apiKey;
    private final Integer pageSize;

    @Autowired
    public YouthPolicyApiClient(
            @Value("${youth-policy.api.base-url}") String baseUrl,
            @Value("${youth-policy.api.policy-path}") String policyPath,
            @Value("${youth-policy.api.key}") String apiKey,
            @Value("${youth-policy.api.page-size}") Integer pageSize
    ) {
        this(new RestTemplate(), new ObjectMapper(), baseUrl, policyPath, apiKey, pageSize);
    }

    YouthPolicyApiClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            String baseUrl,
            String policyPath,
            String apiKey,
            Integer pageSize
    ) {
        this.restTemplate = restTemplate;
        this.restTemplate.getInterceptors().add(youthPolicyRequestInterceptor());
        this.objectMapper = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.baseUrl = baseUrl;
        this.policyPath = policyPath;
        this.apiKey = apiKey;
        this.pageSize = pageSize;
    }

    public YouthPolicyApiResponse getYouthPolicies(int pageNum) {
        return getYouthPolicies(pageNum, null);
    }

    public YouthPolicyApiResponse getYouthPolicies(int pageNum, String policyKeyword) {
        URI requestUri = createRequestUri(pageNum, policyKeyword);

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        requestUri,
                        HttpMethod.GET,
                        new HttpEntity<>(createHeaders()),
                        String.class
                );
                if (responseEntity.getStatusCode().is3xxRedirection()) {
                    log.warn("온통청년 API가 리다이렉트 응답을 반환했습니다. 대상 경로: {}",
                            getRedirectPath(responseEntity.getHeaders()));
                    throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
                }

                String responseBody = responseEntity.getBody();
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
                }

                YouthPolicyApiResponse response = parseResponse(responseBody);
                if (!Integer.valueOf(SUCCESS_RESULT_CODE).equals(response.getResultCode())) {
                    throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
                }
                return response;
            } catch (RestClientException exception) {
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
                }
                waitBeforeRetry(attempt);
            }
        }

        throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
    }

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        return headers;
    }

    private ClientHttpRequestInterceptor youthPolicyRequestInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            request.getHeaders().set(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
            request.getHeaders().set(HttpHeaders.USER_AGENT, USER_AGENT);
            return execution.execute(request, body);
        };
    }

    private YouthPolicyApiResponse parseResponse(String responseBody) {
        if (responseBody.trim().startsWith("<")) {
            return parseXmlResponse(responseBody);
        }

        try {
            return objectMapper.readValue(responseBody, YouthPolicyApiResponse.class);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private String getRedirectPath(HttpHeaders headers) {
        URI location = headers.getLocation();
        return location == null ? "알 수 없음" : location.getPath();
    }

    private YouthPolicyApiResponse parseXmlResponse(String responseBody) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder().parse(
                    new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8))
            );
            Element root = document.getDocumentElement();
            Element result = childElement(root, "result");

            return new YouthPolicyApiResponse(
                    toInteger(childText(root, "resultCode")),
                    childText(root, "resultMessage"),
                    result == null ? null : new YouthPolicyApiResult(
                            toPaging(childElement(result, "pagging")),
                            toYouthPolicyItems(result)
                    )
            );
        } catch (Exception exception) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private YouthPolicyApiPaging toPaging(Element paging) {
        if (paging == null) {
            return null;
        }
        return new YouthPolicyApiPaging(
                toInteger(childText(paging, "totCount")),
                toInteger(childText(paging, "pageNum")),
                toInteger(childText(paging, "pageSize"))
        );
    }

    private List<YouthPolicyApiItem> toYouthPolicyItems(Element result) {
        List<YouthPolicyApiItem> items = new ArrayList<>();
        NodeList elements = result.getElementsByTagName("youthPolicyList");

        for (int index = 0; index < elements.getLength(); index++) {
            Element element = (Element) elements.item(index);
            if (childElement(element, "plcyNo") == null) {
                continue;
            }
            items.add(new YouthPolicyApiItem(
                    childText(element, "plcyNo"), childText(element, "plcyNm"),
                    childText(element, "plcyKywdNm"), childText(element, "plcyExplnCn"),
                    childText(element, "plcySprtCn"), childText(element, "sprvsnInstCd"),
                    childText(element, "sprvsnInstCdNm"), childText(element, "aplyYmd"),
                    childText(element, "plcyAplyMthdCn"), childText(element, "aplyUrlAddr"),
                    childText(element, "refUrlAddr1"), childText(element, "sprtTrgtMinAge"),
                    childText(element, "sprtTrgtMaxAge"), childText(element, "earnCndSeCd"),
                    childText(element, "earnMinAmt"), childText(element, "earnMaxAmt"),
                    childText(element, "earnEtcCn"), childText(element, "addAplyQlfcCndCn"),
                    childText(element, "ptcpPrpTrgtCn"), childText(element, "lastMdfcnDt"),
                    childText(element, "zipCd")
            ));
        }
        return items;
    }

    private Element childElement(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }

    private String childText(Element parent, String name) {
        Element child = childElement(parent, name);
        return child == null ? null : child.getTextContent();
    }

    private Integer toInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private URI createRequestUri(int pageNum, String policyKeyword) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(policyPath)
                .queryParam("apiKeyNm", apiKey)
                .queryParam("pageNum", pageNum)
                .queryParam("rtnType", "json");

        if (policyKeyword != null && !policyKeyword.isBlank()) {
            builder.queryParam("plcyKywdNm", policyKeyword);
        }

        return builder.build().encode().toUri();
    }
}
