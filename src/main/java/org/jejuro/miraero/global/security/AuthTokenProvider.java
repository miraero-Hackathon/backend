package org.jejuro.miraero.global.security;

public interface AuthTokenProvider {

  String createAccessToken(Long userId);

  String createRefreshToken(Long userId);

  Long getAccessTokenExpiresIn();

  Long getRefreshTokenExpiresIn();

  boolean validateToken(String token);

  Long getUserId(String token);

  boolean isAccessToken(String token);

  boolean isRefreshToken(String token);
}
