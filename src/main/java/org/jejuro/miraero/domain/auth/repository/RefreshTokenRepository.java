package org.jejuro.miraero.domain.auth.repository;

public interface RefreshTokenRepository {

  void save(Long userId, String refreshToken, Long expiresIn);

  String findByUserId(Long userId);

  void deleteByUserId(Long userId);

}
