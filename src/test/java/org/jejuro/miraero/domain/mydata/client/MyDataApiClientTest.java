package org.jejuro.miraero.domain.mydata.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataAccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class MyDataApiClientTest {

  private static final String BASE_URL = "http://localhost:9000";

  private MockRestServiceServer mockServer;
  private MyDataApiClient myDataApiClient;

  @BeforeEach
  void setUp() {
    RestTemplate restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    myDataApiClient = new MyDataApiClient(
        restTemplate,
        new ObjectMapper(),
        BASE_URL,
        "/mock/accounts",
        "/mock/transactions"
    );
  }

  @Test
  @DisplayName("Bearer 토큰을 붙여 계좌 목록을 조회한다")
  void getAccounts() {
    mockServer.expect(requestTo(BASE_URL + "/mock/accounts/10001"))
        .andExpect(header("Authorization", "Bearer token-1"))
        .andRespond(withSuccess(
            "[{\"accountId\":201,\"kbUserId\":10001,\"financialInstitutionCode\":\"004\","
                + "\"accountType\":\"CHECKING\",\"accountName\":\"KB 입출금통장\","
                + "\"accountNumber\":\"1234567890\",\"balance\":3400000,"
                + "\"accountStatus\":\"ACTIVE\",\"openedAt\":\"2020-01-01\","
                + "\"maturityAt\":null,\"interestRate\":0.1,\"monthlyPaymentLimit\":null}]",
            MediaType.APPLICATION_JSON));

    List<MyDataAccountResponse> accounts = myDataApiClient.getAccounts(10001L, "token-1");

    assertEquals(1, accounts.size());
    assertEquals(201L, accounts.get(0).getAccountId());
    assertEquals("004", accounts.get(0).getFinancialInstitutionCode());
    mockServer.verify();
  }
}
