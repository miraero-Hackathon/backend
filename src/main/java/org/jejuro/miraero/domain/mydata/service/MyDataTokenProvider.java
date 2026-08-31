package org.jejuro.miraero.domain.mydata.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.mydata.client.MyDataAuthClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.domain.mydata.mapper.MyDataConsentMapper;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.mydata.repository.MyDataTokenRepository;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 마이데이터 액세스 토큰 발급·보관을 담당한다.
 *
 * 연동(MyDataConnectService)과 이체(AccountTransferService) 양쪽이 같은 재인증
 * 로직을 필요로 하는데, 이체 서비스가 연동 서비스를 직접 참조하면 순환 참조가
 * 생기므로 공통 부분만 별도 빈으로 분리했다.
 */
@Component
@RequiredArgsConstructor
public class MyDataTokenProvider {

  // mock-server가 지금은 국민은행 하나만 시뮬레이션하므로 고정 코드로 조회한다
  private static final String MOCK_INSTITUTION_CODE = "004";
  private static final String CONNECTION_STATUS_CONNECTED = "CONNECTED";

  private final MyDataAuthClient myDataAuthClient;
  private final MyDataTokenRepository myDataTokenRepository;
  private final UserMapper userMapper;
  private final ReferenceDataMapper referenceDataMapper;
  private final MyDataConsentMapper myDataConsentMapper;

  /**
   * 보관 중인 토큰을 돌려주되, 없으면 재인증해서 새로 발급받는다.
   *
   * 토큰 수명이 1시간이고 mock-server는 토큰을 메모리에 들고 있어 재시작 시에도
   * 무효화된다. 조회는 동기화해둔 로컬 데이터로 되지만 이체는 매번 외부 호출이
   * 필요하므로, 사용자에게 재연동을 요구하는 대신 여기서 자동으로 복구한다.
   */
  public String getValidToken(Long userId) {
    String token = myDataTokenRepository.findByUserId(userId);
    if (token != null) {
      return token;
    }

    User user = findConnectedUser(userId);
    return authenticateAndPersist(userId, user).getAccessToken();
  }

  /**
   * 재인증 후 토큰·프로필·연동상태를 저장한다.
   */
  public MyDataTokenResponse authenticateAndPersist(Long userId, User user) {
    String authorizationCode = myDataAuthClient.requestAuthorizationCode(user.getEmail());
    MyDataTokenResponse token = myDataAuthClient.exchangeToken(authorizationCode);

    myDataTokenRepository.save(userId, token.getAccessToken(), token.getExpiresIn());
    userMapper.updateKbPayId(userId, token.getKbUserId());
    userMapper.updateProfile(
        userId, token.getName(), token.getBirthDate(),
        token.getCompanyName(), token.getMonthlyIncome());

    myDataConsentMapper.upsertConnection(
        userId,
        resolveMockInstitutionId(),
        CONNECTION_STATUS_CONNECTED,
        LocalDateTime.now().plusSeconds(token.getExpiresIn())
    );

    return token;
  }

  public Long resolveMockInstitutionId() {
    Long financialInstitutionId =
        referenceDataMapper.findFinancialInstitutionIdByCode(MOCK_INSTITUTION_CODE);

    if (financialInstitutionId == null) {
      throw new BusinessException(MyDataErrorCode.MYDATA_INSTITUTION_NOT_FOUND);
    }
    return financialInstitutionId;
  }

  /**
   * 한 번도 연동한 적 없는 사용자는 재인증 대상이 아니다.
   * kb_pay_id가 없으면 외부 계정 자체를 특정할 수 없다.
   */
  private User findConnectedUser(Long userId) {
    User user = userMapper.findById(userId);

    if (user == null || user.getKbPayId() == null) {
      throw new BusinessException(MyDataErrorCode.MYDATA_NOT_CONNECTED);
    }
    return user;
  }
}
