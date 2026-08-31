package org.jejuro.miraero.global.security;

import lombok.Getter;

@Getter
public class AuthenticatedUser {

  private final Long userId;

  public AuthenticatedUser(Long userId) {
    this.userId = userId;
  }
}
