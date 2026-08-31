package org.jejuro.miraero.domain.mydata.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataAuthorizeResponse;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MyDataAuthClient {

  private static final String CLIENT_SECRET_HEADER = "X-Client-Secret";

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;
  private final String authorizePath;
  private final String tokenPath;
  private final String clientSecret;

  @Autowired
  public MyDataAuthClient(
      @Value("${mydata.api.base-url}") String baseUrl,
      @Value("${mydata.api.authorize-path}") String authorizePath,
      @Value("${mydata.api.token-path}") String tokenPath,
      @Value("${mydata.oauth.client-secret}") String clientSecret
  ) {
    this(new RestTemplate(), new ObjectMapper(), baseUrl, authorizePath, tokenPath, clientSecret);
  }

  MyDataAuthClient(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      String baseUrl,
      String authorizePath,
      String tokenPath,
      String clientSecret
  ) {
    this.restTemplate = restTemplate;
    // MyDataTokenResponse.birthDate(LocalDate) 역직렬화를 위해 필요
    this.objectMapper = objectMapper.registerModule(new JavaTimeModule());
    this.baseUrl = baseUrl;
    this.authorizePath = authorizePath;
    this.tokenPath = tokenPath;
    this.clientSecret = clientSecret;
  }

  public String requestAuthorizationCode(String email) {
    MyDataAuthorizeResponse response = post(
        authorizePath,
        Map.of("email", email),
        MyDataAuthorizeResponse.class
    );
    return response.getAuthorizationCode();
  }

  public MyDataTokenResponse exchangeToken(String authorizationCode) {
    return post(
        tokenPath,
        Map.of("authorizationCode", authorizationCode),
        MyDataTokenResponse.class
    );
  }

  private <T> T post(String path, Map<String, String> body, Class<T> responseType) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    // mock-server는 이 헤더 없이는 모든 mydata oauth 요청을 거부 (401 에러)
    headers.set(CLIENT_SECRET_HEADER, clientSecret);

    try {
      String requestBody = objectMapper.writeValueAsString(body);
      String responseBody = restTemplate.postForObject(
          baseUrl + path,
          new HttpEntity<>(requestBody, headers),
          String.class
      );

      if (responseBody == null) {
        throw new BusinessException(MyDataErrorCode.MYDATA_LINK_FAILED);
      }
      return objectMapper.readValue(responseBody, responseType);
    } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException exception) {
      throw new BusinessException(MyDataErrorCode.MYDATA_LINK_FAILED);
    }
  }
}
