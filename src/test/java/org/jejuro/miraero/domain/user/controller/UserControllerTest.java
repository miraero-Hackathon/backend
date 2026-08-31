package org.jejuro.miraero.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jejuro.miraero.domain.user.dto.request.PasswordChangeRequest;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;
import org.jejuro.miraero.domain.user.exception.UserErrorCode;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private static final Long USER_ID = 1L;

  @Mock
  private UserService userService;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    UserController userController = new UserController(userService);

    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders
        .standaloneSetup(userController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();

    SecurityContextHolder.getContext().setAuthentication(
        new JwtAuthenticationToken(new AuthenticatedUser(USER_ID))
    );

    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("getProfile returns profile")
  void getProfile_success() throws Exception {
    ProfileResponse response = ProfileResponse.builder()
        .userId(USER_ID)
        .name("Mirae Kim")
        .email("miraero@gmail.com")
        .birthDate("2001-03-15")
        .profileImageUrl(null)
        .company("KB Financial Group")
        .monthlyIncome(2_850_000L)
        .kbpayLinked(true)
        .build();
    given(userService.getProfile(USER_ID)).willReturn(response);

    mockMvc.perform(get("/api/users/profile"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value(USER_ID))
        .andExpect(jsonPath("$.data.name").value("Mirae Kim"))
        .andExpect(jsonPath("$.data.email").value("miraero@gmail.com"))
        .andExpect(jsonPath("$.data.birthDate").value("2001-03-15"))
        .andExpect(jsonPath("$.data.profileImageUrl").doesNotExist())
        .andExpect(jsonPath("$.data.company").value("KB Financial Group"))
        .andExpect(jsonPath("$.data.monthlyIncome").value(2_850_000L))
        .andExpect(jsonPath("$.data.kbpayLinked").value(true));

    verify(userService).getProfile(USER_ID);
  }

  @Test
  @DisplayName("getProfile returns 404 when user does not exist")
  void getProfile_notFound() throws Exception {
    given(userService.getProfile(USER_ID))
        .willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

    mockMvc.perform(get("/api/users/profile"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_004"));

    verify(userService).getProfile(USER_ID);
  }

  @Test
  @DisplayName("changePassword returns 200")
  void changePassword_success() throws Exception {
    PasswordChangeRequest request = new PasswordChangeRequest(
        "Current123!",
        "NewPassword123!",
        "NewPassword123!"
    );

    mockMvc.perform(patch("/api/users/me/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").doesNotExist())
        .andExpect(jsonPath("$.error").doesNotExist());

    verify(userService).changePassword(any(Long.class), any(PasswordChangeRequest.class));
  }

  @Test
  @DisplayName("changePassword returns 400 when current password mismatches")
  void changePassword_currentPasswordMismatch() throws Exception {
    PasswordChangeRequest request = new PasswordChangeRequest(
        "Wrong123!",
        "NewPassword123!",
        "NewPassword123!"
    );
    willThrow(new BusinessException(UserErrorCode.CURRENT_PASSWORD_MISMATCH))
        .given(userService)
        .changePassword(any(Long.class), any(PasswordChangeRequest.class));

    mockMvc.perform(patch("/api/users/me/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("USER_002"));

    verify(userService).changePassword(any(Long.class), any(PasswordChangeRequest.class));
  }

  @Test
  @DisplayName("changePassword returns 400 when request is invalid")
  void changePassword_blankCurrentPassword() throws Exception {
    PasswordChangeRequest request = new PasswordChangeRequest(
        "",
        "NewPassword123!",
        "NewPassword123!"
    );

    mockMvc.perform(patch("/api/users/me/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_002"));
  }
}
