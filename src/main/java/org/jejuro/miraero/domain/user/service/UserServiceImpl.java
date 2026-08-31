package org.jejuro.miraero.domain.user.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.mydata.service.MyDataConsentService;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.dto.request.PasswordChangeRequest;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;
import org.jejuro.miraero.domain.user.exception.UserErrorCode;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final String MOCK_NAME = "테스트 사용자";
  private static final LocalDate MOCK_BIRTH_DATE = LocalDate.of(2000, 1, 1);
  private static final String MOCK_COMPANY_NAME = "테스트 회사";
  private static final long MOCK_MONTHLY_INCOME = 3_000_000L;
  private static final long MOCK_KB_PAY_ID = 1L;

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final MyDataConsentService myDataConsentService;

  @Override
  @Transactional
  public User create(UserCreateCommand command) {
    boolean exists = userMapper.existsByEmail(command.getEmail());

    if (exists) {
      throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }

    String passwordHash = passwordEncoder.encode(command.getPassword());

    User user = User.create(
        MOCK_NAME,
        MOCK_BIRTH_DATE,
        MOCK_COMPANY_NAME,
        MOCK_MONTHLY_INCOME,
        command.getEmail(),
        passwordHash,
        MOCK_KB_PAY_ID
    );

    userMapper.save(user);
    myDataConsentService.createInitialConsent(user.getUserId());

    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public ProfileResponse getProfile(Long userId) {
    ProfileResponse response = userMapper.findProfileById(userId);

    if (response == null) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    return response;
  }

  @Override
  @Transactional
  public void changePassword(Long userId, PasswordChangeRequest request) {
    User user = userMapper.findById(userId);

    if (user == null) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
      throw new BusinessException(UserErrorCode.CURRENT_PASSWORD_MISMATCH);
    }

    if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
      throw new BusinessException(UserErrorCode.NEW_PASSWORD_CONFIRM_MISMATCH);
    }

    String passwordHash = passwordEncoder.encode(request.getNewPassword());
    userMapper.updatePasswordHash(userId, passwordHash);
  }

  @Override
  @Transactional(readOnly = true)
  public Long getMonthlyIncome(Long userId) {
    return userMapper.findMonthlyIncome(userId);
  }
}

