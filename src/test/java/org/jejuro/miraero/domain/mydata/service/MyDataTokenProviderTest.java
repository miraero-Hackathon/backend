package org.jejuro.miraero.domain.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.mydata.client.MyDataAuthClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.domain.mydata.mapper.MyDataConsentMapper;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.mydata.repository.MyDataTokenRepository;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MyDataTokenProviderTest {

  private static final Long USER_ID = 1L;

  @Mock
  private MyDataAuthClient myDataAuthClient;
  @Mock
  private MyDataTokenRepository myDataTokenRepository;
  @Mock
  private UserMapper userMapper;
  @Mock
  private ReferenceDataMapper referenceDataMapper;
  @Mock
  private MyDataConsentMapper myDataConsentMapper;

  @InjectMocks
  private MyDataTokenProvider myDataTokenProvider;

  @Test
  @DisplayName("보관 중인 토큰이 있으면 재인증 없이 그대로 쓴다")
  void getValidToken_reusesStoredToken() {
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn("stored-token");

    assertEquals("stored-token", myDataTokenProvider.getValidToken(USER_ID));

    verify(myDataAuthClient, never()).requestAuthorizationCode(anyString());
  }

  @Test
  @DisplayName("토큰이 만료됐으면 재인증해서 새로 발급받는다")
  void getValidToken_reissuesWhenExpired() {
    User user = connectedUser();
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn(null);
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(myDataAuthClient.requestAuthorizationCode("miraero01@test.com")).thenReturn("code-1");
    when(myDataAuthClient.exchangeToken("code-1")).thenReturn(token("new-token"));
    when(referenceDataMapper.findFinancialInstitutionIdByCode("004")).thenReturn(1L);

    assertEquals("new-token", myDataTokenProvider.getValidToken(USER_ID));

    verify(myDataTokenRepository).save(USER_ID, "new-token", 3600L);
  }

  @Test
  @DisplayName("한 번도 연동한 적 없으면 재인증 대상이 아니다")
  void getValidToken_neverConnected() {
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn(null);
    // kbPayId가 없는 사용자 — 외부 계정을 특정할 수 없다
    when(userMapper.findById(USER_ID))
        .thenReturn(User.create(null, null, null, null, "x@test.com", "hash", null));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> myDataTokenProvider.getValidToken(USER_ID));

    assertEquals(MyDataErrorCode.MYDATA_NOT_CONNECTED, exception.getErrorCode());
    verify(myDataAuthClient, never()).requestAuthorizationCode(anyString());
  }

  @Test
  @DisplayName("재인증 시 프로필과 연동 상태도 함께 갱신한다")
  void authenticateAndPersist_updatesProfile() {
    User user = connectedUser();
    when(myDataAuthClient.requestAuthorizationCode("miraero01@test.com")).thenReturn("code-1");
    when(myDataAuthClient.exchangeToken("code-1")).thenReturn(token("new-token"));
    when(referenceDataMapper.findFinancialInstitutionIdByCode("004")).thenReturn(1L);

    myDataTokenProvider.authenticateAndPersist(USER_ID, user);

    verify(userMapper).updateKbPayId(USER_ID, 10001L);
    verify(userMapper).updateProfile(USER_ID, "탁민주", null, "중견기업J", 2_850_000L);
  }

  private User connectedUser() {
    User user = User.create(null, null, null, null, "miraero01@test.com", "hash", null);
    ReflectionTestUtils.setField(user, "kbPayId", 10001L);
    return user;
  }

  private MyDataTokenResponse token(String accessToken) {
    MyDataTokenResponse token = new MyDataTokenResponse();
    ReflectionTestUtils.setField(token, "accessToken", accessToken);
    ReflectionTestUtils.setField(token, "expiresIn", 3600L);
    ReflectionTestUtils.setField(token, "kbUserId", 10001L);
    ReflectionTestUtils.setField(token, "name", "탁민주");
    ReflectionTestUtils.setField(token, "monthlyIncome", 2_850_000L);
    ReflectionTestUtils.setField(token, "companyName", "중견기업J");
    return token;
  }
}
