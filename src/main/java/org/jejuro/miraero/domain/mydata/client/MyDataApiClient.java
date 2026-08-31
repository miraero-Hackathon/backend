package org.jejuro.miraero.domain.mydata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataAccountResponse;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTransactionResponse;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTransferRequest;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTransferResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MyDataApiClient {

  private static final String BEARER_PREFIX = "Bearer ";

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;
  private final String accountsPath;
  private final String transactionsPath;

  @Autowired
  public MyDataApiClient(
      @Value("${mydata.api.base-url}") String baseUrl,
      @Value("${mydata.api.accounts-path}") String accountsPath,
      @Value("${mydata.api.transactions-path}") String transactionsPath
  ) {
    this(new RestTemplate(), new ObjectMapper(), baseUrl, accountsPath, transactionsPath);
  }

  MyDataApiClient(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      String baseUrl,
      String accountsPath,
      String transactionsPath
  ) {
    this.restTemplate = restTemplate;
    // 외부 API 응답 DTO의 LocalDate/LocalDateTime 필드 역직렬화를 위해 필요
    this.objectMapper = objectMapper.registerModule(new JavaTimeModule());
    this.baseUrl = baseUrl;
    this.accountsPath = accountsPath;
    this.transactionsPath = transactionsPath;
  }

  public List<MyDataAccountResponse> getAccounts(Long kbUserId, String accessToken) {
    return get(accountsPath, kbUserId, accessToken, new TypeReference<List<MyDataAccountResponse>>() {
    });
  }

  public List<MyDataTransactionResponse> getTransactions(Long kbUserId, String accessToken) {
    return get(transactionsPath, kbUserId, accessToken,
        new TypeReference<List<MyDataTransactionResponse>>() {
        });
  }

  /**
   * 계좌 간 실제 이체를 요청한다. 로컬 잔액만 바꾸면 다음 동기화 때 mock 서버의
   * 옛 값으로 덮어써지므로, 계좌를 넘나드는 자금 이동은 반드시 이 메서드를 거쳐야 한다.
   */
  public MyDataTransferResponse transfer(MyDataTransferRequest request, String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    try {
      ResponseEntity<String> response = restTemplate.exchange(
          baseUrl + transactionsPath + "/transfers",
          HttpMethod.POST,
          new HttpEntity<>(objectMapper.writeValueAsString(request), headers),
          String.class
      );

      String responseBody = response.getBody();
      if (responseBody == null) {
        throw new BusinessException(MyDataErrorCode.MYDATA_TRANSFER_FAILED);
      }
      return objectMapper.readValue(responseBody, MyDataTransferResponse.class);
    } catch (RestClientException | JsonProcessingException exception) {
      throw new BusinessException(MyDataErrorCode.MYDATA_TRANSFER_FAILED);
    }
  }

  private <T> T get(String path, Long kbUserId, String accessToken, TypeReference<T> typeReference) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);

    try {
      ResponseEntity<String> response = restTemplate.exchange(
          baseUrl + path + "/" + kbUserId,
          HttpMethod.GET,
          new HttpEntity<>(headers),
          String.class
      );

      String responseBody = response.getBody();
      if (responseBody == null) {
        throw new BusinessException(MyDataErrorCode.MYDATA_SYNC_FAILED);
      }
      return objectMapper.readValue(responseBody, typeReference);
    } catch (RestClientException | JsonProcessingException exception) {
      throw new BusinessException(MyDataErrorCode.MYDATA_SYNC_FAILED);
    }
  }
}
