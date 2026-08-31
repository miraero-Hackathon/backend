package org.jejuro.miraero.domain.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.domain.mydata.dto.response.MyDataConnectResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.domain.mydata.mapper.MyDataConsentMapper;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MyDataConnectServiceImplTest {

  private static final Long USER_ID = 1L;
  private static final Long INSTITUTION_ID = 1L;

  @Mock
  private MyDataSyncService myDataSyncService;
  @Mock
  private UserMapper userMapper;
  @Mock
  private MyDataConsentMapper myDataConsentMapper;
  @Mock
  private MyDataTokenProvider myDataTokenProvider;

  private MyDataConnectService myDataConnectService;

  @BeforeEach
  void setUp() {
    myDataConnectService = new MyDataConnectServiceImpl(
        myDataSyncService, userMapper, myDataConsentMapper, myDataTokenProvider);
  }

  @Test
  @DisplayName("연동에 성공하면 kbUserId를 반환한다")
  void connect() {
    User user = createUser("miraero01@test.com");
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(myDataTokenProvider.authenticateAndPersist(USER_ID, user))
        .thenReturn(createToken("token-1", 3600L, 10001L));

    MyDataConnectResponse response = myDataConnectService.connect(USER_ID);

    assertEquals(10001L, response.getKbUserId());
    verify(myDataTokenProvider).authenticateAndPersist(USER_ID, user);
  }

  @Test
  @DisplayName("존재하지 않는 사용자면 예외를 던진다")
  void connect_userNotFound() {
    when(userMapper.findById(USER_ID)).thenReturn(null);

    assertThrows(BusinessException.class, () -> myDataConnectService.connect(USER_ID));
  }

  @Test
  @DisplayName("연동 이력이 없으면 동기화할 수 없다")
  void sync_notConnected() {
    when(userMapper.findById(USER_ID)).thenReturn(createUser("miraero01@test.com"));

    BusinessException exception =
        assertThrows(BusinessException.class, () -> myDataConnectService.sync(USER_ID));

    assertEquals(MyDataErrorCode.MYDATA_NOT_CONNECTED, exception.getErrorCode());
  }

  @Test
  @DisplayName("동기화 성공 시 synced_at을 갱신한다")
  void sync_updatesSyncedAt() {
    User user = createUser("miraero01@test.com");
    ReflectionTestUtils.setField(user, "kbPayId", 10001L);
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(myDataTokenProvider.resolveMockInstitutionId()).thenReturn(INSTITUTION_ID);

    myDataConnectService.sync(USER_ID);

    verify(myDataSyncService).sync(USER_ID, 10001L);
    verify(myDataConsentMapper).updateSyncedAt(USER_ID, INSTITUTION_ID);
  }

  @Test
  @DisplayName("토큰이 만료됐으면 재인증 후 한 번만 재시도한다")
  void sync_refreshesExpiredToken() {
    User user = createUser("miraero01@test.com");
    ReflectionTestUtils.setField(user, "kbPayId", 10001L);
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(myDataTokenProvider.resolveMockInstitutionId()).thenReturn(INSTITUTION_ID);

    doThrow(new BusinessException(MyDataErrorCode.MYDATA_TOKEN_EXPIRED))
        .doNothing()
        .when(myDataSyncService).sync(anyLong(), anyLong());

    myDataConnectService.sync(USER_ID);

    verify(myDataSyncService, times(2)).sync(anyLong(), anyLong());
    verify(myDataTokenProvider).authenticateAndPersist(USER_ID, user);
  }

  @Test
  @DisplayName("토큰 만료가 아닌 오류는 재시도하지 않고 그대로 던진다")
  void sync_otherError_notRetried() {
    User user = createUser("miraero01@test.com");
    ReflectionTestUtils.setField(user, "kbPayId", 10001L);
    when(userMapper.findById(USER_ID)).thenReturn(user);

    doThrow(new BusinessException(MyDataErrorCode.MYDATA_SYNC_FAILED))
        .when(myDataSyncService).sync(anyLong(), anyLong());

    assertThrows(BusinessException.class, () -> myDataConnectService.sync(USER_ID));

    verify(myDataSyncService, times(1)).sync(anyLong(), anyLong());
    verify(myDataTokenProvider, times(0)).authenticateAndPersist(anyLong(), any());
  }

  private User createUser(String email) {
    return User.create(null, null, null, null, email, "hash", null);
  }

  private MyDataTokenResponse createToken(String accessToken, Long expiresIn, Long kbUserId) {
    MyDataTokenResponse token = new MyDataTokenResponse();
    ReflectionTestUtils.setField(token, "accessToken", accessToken);
    ReflectionTestUtils.setField(token, "expiresIn", expiresIn);
    ReflectionTestUtils.setField(token, "kbUserId", kbUserId);
    return token;
  }
}
