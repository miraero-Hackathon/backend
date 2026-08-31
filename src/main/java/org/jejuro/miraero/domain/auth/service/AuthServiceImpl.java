package org.jejuro.miraero.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.auth.dto.request.LoginRequest;
import org.jejuro.miraero.domain.auth.dto.request.SignUpRequest;
import org.jejuro.miraero.domain.auth.dto.response.LoginResponse;
import org.jejuro.miraero.domain.auth.dto.response.LoginUserResponse;
import org.jejuro.miraero.domain.auth.dto.response.SignUpResponse;
import org.jejuro.miraero.domain.auth.dto.response.TokenReissueResponse;
import org.jejuro.miraero.domain.auth.exception.AuthErrorCode;
import org.jejuro.miraero.domain.auth.repository.RefreshTokenRepository;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.domain.user.service.UserCreateCommand;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.security.AuthTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

  private static final boolean AUTO_LOGIN = true;

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenProvider authTokenProvider;
  private final UserService userService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final GoalMapper goalMapper;

  @Override
  @Transactional
  public SignUpResponse signUp(SignUpRequest request) {
    User user = userService.create(
        new UserCreateCommand(
            request.getEmail(),
            request.getPassword()
        )
    );

    return SignUpResponse.from(user);
  }

  @Override
  @Transactional
  public LoginResponse login(LoginRequest request) {
    User user = userMapper.findByEmail(request.getEmail());

    if (user == null) {
      throw new BusinessException(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BusinessException(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    Long userId = user.getUserId();
    String accessToken = authTokenProvider.createAccessToken(userId);
    String refreshToken = authTokenProvider.createRefreshToken(userId);
    Long accessTokenExpiresIn = authTokenProvider.getAccessTokenExpiresIn();
    Long refreshTokenExpiresIn = authTokenProvider.getRefreshTokenExpiresIn();

    refreshTokenRepository.save(
        userId,
        refreshToken,
        refreshTokenExpiresIn
    );

    return new LoginResponse(
        accessToken,
        refreshToken,
        accessTokenExpiresIn,
        refreshTokenExpiresIn,
        AUTO_LOGIN,
        LoginUserResponse.from(user, goalMapper.existsActiveGoalByUserId(userId))
    );
  }

  @Override
  @Transactional
  public TokenReissueResponse reissue(String refreshToken) {
    if (!authTokenProvider.validateToken(refreshToken)
        || !authTokenProvider.isRefreshToken(refreshToken)) {
      throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    Long userId = authTokenProvider.getUserId(refreshToken);
    String savedRefreshToken = refreshTokenRepository.findByUserId(userId);

    if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
      throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    User user = userMapper.findById(userId);

    if (user == null) {
      throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    String newAccessToken = authTokenProvider.createAccessToken(user.getUserId());
    String newRefreshToken = authTokenProvider.createRefreshToken(user.getUserId());

    Long accessTokenExpiresIn = authTokenProvider.getAccessTokenExpiresIn();
    Long refreshTokenExpiresIn = authTokenProvider.getRefreshTokenExpiresIn();

    refreshTokenRepository.save(
        user.getUserId(),
        newRefreshToken,
        refreshTokenExpiresIn
    );

    return new TokenReissueResponse(
        newAccessToken,
        newRefreshToken,
        accessTokenExpiresIn,
        refreshTokenExpiresIn,
        LoginUserResponse.from(user, goalMapper.existsActiveGoalByUserId(user.getUserId()))
    );
  }

  @Override
  @Transactional
  public void logout(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }
}
