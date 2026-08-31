package org.jejuro.miraero.domain.mydata.repository;

public interface MyDataTokenRepository {

  void save(Long userId, String accessToken, Long expiresIn);

  String findByUserId(Long userId);

  void deleteByUserId(Long userId);
}
