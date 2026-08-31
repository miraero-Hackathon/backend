package org.jejuro.miraero.domain.user.service;

import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.dto.request.PasswordChangeRequest;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;

public interface UserService {

  User create(UserCreateCommand command);

  ProfileResponse getProfile(Long userId);

  void changePassword(Long userId, PasswordChangeRequest request);

  Long getMonthlyIncome(Long userId);
}
