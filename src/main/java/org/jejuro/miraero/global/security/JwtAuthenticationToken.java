package org.jejuro.miraero.global.security;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  private final AuthenticatedUser principal;

  public JwtAuthenticationToken(AuthenticatedUser principal) {
    super(Collections.emptyList());
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return "";
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }
}
