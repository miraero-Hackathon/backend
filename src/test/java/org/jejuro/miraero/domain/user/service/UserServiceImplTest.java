package org.jejuro.miraero.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.jejuro.miraero.domain.mydata.service.MyDataConsentService;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.dto.request.PasswordChangeRequest;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;
import org.jejuro.miraero.domain.user.exception.UserErrorCode;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MyDataConsentService myDataConsentService;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(
        userMapper,
        passwordEncoder,
        myDataConsentService
    );
  }

  @Test
  @DisplayName("create saves user and initial mydata consent")
  void create_success() {
    UserCreateCommand command =
        new UserCreateCommand("test@example.com", "password123!");

    when(userMapper.existsByEmail(command.getEmail()))
        .thenReturn(false);
    when(passwordEncoder.encode(command.getPassword()))
        .thenReturn("encodedPassword");
    when(userMapper.save(any(User.class)))
        .thenReturn(1);

    User user = userService.create(command);

    assertNotNull(user.getName());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("encodedPassword", user.getPasswordHash());

    verify(userMapper).existsByEmail("test@example.com");
    verify(passwordEncoder).encode("password123!");
    verify(userMapper).save(any(User.class));
    verify(myDataConsentService).createInitialConsent(user.getUserId());
  }

  @Test
  @DisplayName("create throws duplicate email error")
  void create_duplicateEmail() {
    UserCreateCommand command =
        new UserCreateCommand("test@example.com", "password123!");

    when(userMapper.existsByEmail(command.getEmail()))
        .thenReturn(true);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> userService.create(command)
    );

    assertEquals(UserErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
    verify(userMapper).existsByEmail("test@example.com");
  }

  @Test
  @DisplayName("getProfile returns profile")
  void getProfile_success() {
    Long userId = 1L;
    ProfileResponse response = ProfileResponse.builder()
        .userId(userId)
        .name("Mirae Kim")
        .email("miraero@gmail.com")
        .birthDate("2001-03-15")
        .profileImageUrl(null)
        .company("KB Financial Group")
        .monthlyIncome(2_850_000L)
        .kbpayLinked(true)
        .build();
    when(userMapper.findProfileById(userId)).thenReturn(response);

    ProfileResponse result = userService.getProfile(userId);

    assertEquals(userId, result.getUserId());
    assertEquals("Mirae Kim", result.getName());
    assertEquals("miraero@gmail.com", result.getEmail());
    assertEquals("2001-03-15", result.getBirthDate());
    assertEquals("KB Financial Group", result.getCompany());
    assertEquals(2_850_000L, result.getMonthlyIncome());
    assertEquals(true, result.isKbpayLinked());
    verify(userMapper).findProfileById(userId);
  }

  @Test
  @DisplayName("getProfile throws not found")
  void getProfile_notFound() {
    Long userId = 99L;
    when(userMapper.findProfileById(userId)).thenReturn(null);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> userService.getProfile(userId)
    );

    assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    verify(userMapper).findProfileById(userId);
  }

  @Test
  @DisplayName("changePassword encodes and saves new password")
  void changePassword_success() {
    Long userId = 1L;
    User user = createUser("encodedCurrentPassword");
    PasswordChangeRequest request = new PasswordChangeRequest(
        "Current123!",
        "NewPassword123!",
        "NewPassword123!"
    );
    when(userMapper.findById(userId)).thenReturn(user);
    when(passwordEncoder.matches("Current123!", "encodedCurrentPassword")).thenReturn(true);
    when(passwordEncoder.encode("NewPassword123!")).thenReturn("encodedNewPassword");

    userService.changePassword(userId, request);

    verify(userMapper).findById(userId);
    verify(passwordEncoder).matches("Current123!", "encodedCurrentPassword");
    verify(passwordEncoder).encode("NewPassword123!");
    verify(userMapper).updatePasswordHash(userId, "encodedNewPassword");
  }

  @Test
  @DisplayName("changePassword throws not found")
  void changePassword_userNotFound() {
    Long userId = 99L;
    PasswordChangeRequest request = new PasswordChangeRequest(
        "Current123!",
        "NewPassword123!",
        "NewPassword123!"
    );
    when(userMapper.findById(userId)).thenReturn(null);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> userService.changePassword(userId, request)
    );

    assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    verify(userMapper).findById(userId);
    verify(passwordEncoder, never()).matches(any(), any());
    verify(userMapper, never()).updatePasswordHash(any(), any());
  }

  @Test
  @DisplayName("changePassword throws current password mismatch")
  void changePassword_currentPasswordMismatch() {
    Long userId = 1L;
    User user = createUser("encodedCurrentPassword");
    PasswordChangeRequest request = new PasswordChangeRequest(
        "Wrong123!",
        "NewPassword123!",
        "NewPassword123!"
    );
    when(userMapper.findById(userId)).thenReturn(user);
    when(passwordEncoder.matches("Wrong123!", "encodedCurrentPassword")).thenReturn(false);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> userService.changePassword(userId, request)
    );

    assertEquals(UserErrorCode.CURRENT_PASSWORD_MISMATCH, exception.getErrorCode());
    verify(userMapper).findById(userId);
    verify(passwordEncoder).matches("Wrong123!", "encodedCurrentPassword");
    verify(passwordEncoder, never()).encode(any());
    verify(userMapper, never()).updatePasswordHash(any(), any());
  }

  @Test
  @DisplayName("changePassword throws new password confirmation mismatch")
  void changePassword_newPasswordConfirmMismatch() {
    Long userId = 1L;
    User user = createUser("encodedCurrentPassword");
    PasswordChangeRequest request = new PasswordChangeRequest(
        "Current123!",
        "NewPassword123!",
        "Different123!"
    );
    when(userMapper.findById(userId)).thenReturn(user);
    when(passwordEncoder.matches("Current123!", "encodedCurrentPassword")).thenReturn(true);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> userService.changePassword(userId, request)
    );

    assertEquals(UserErrorCode.NEW_PASSWORD_CONFIRM_MISMATCH, exception.getErrorCode());
    verify(userMapper).findById(userId);
    verify(passwordEncoder).matches("Current123!", "encodedCurrentPassword");
    verify(passwordEncoder, never()).encode(any());
    verify(userMapper, never()).updatePasswordHash(any(), any());
  }

  private User createUser(String passwordHash) {
    return User.create(
        "Test User",
        LocalDate.of(2000, 1, 1),
        "Test Company",
        3_000_000L,
        "test@example.com",
        passwordHash,
        1L
    );
  }
}