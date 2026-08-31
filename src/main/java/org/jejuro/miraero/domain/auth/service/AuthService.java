package org.jejuro.miraero.domain.auth.service;

import org.jejuro.miraero.domain.auth.dto.request.LoginRequest;
import org.jejuro.miraero.domain.auth.dto.request.SignUpRequest;
import org.jejuro.miraero.domain.auth.dto.response.LoginResponse;
import org.jejuro.miraero.domain.auth.dto.response.SignUpResponse;
import org.jejuro.miraero.domain.auth.dto.response.TokenReissueResponse;

public interface AuthService {

  SignUpResponse signUp(SignUpRequest request);

  LoginResponse login(LoginRequest request);

  TokenReissueResponse reissue(String refreshToken);

  void logout(Long userId);
}
