package org.jejuro.miraero.global.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  private final AuthTokenProvider authTokenProvider;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain
  ) throws ServletException, IOException {
    String token = resolveToken(request);

    if (isValidAccessToken(token)) {
      saveAuthentication(token);
    }

    chain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String authorization = request.getHeader(AUTHORIZATION_HEADER);

    if (authorization == null || !authorization.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
      return null;
    }

    return authorization.substring(AUTHORIZATION_HEADER_PREFIX.length());
  }

  private boolean isValidAccessToken(String token) {
    return token != null
        && authTokenProvider.validateToken(token)
        && authTokenProvider.isAccessToken(token);
  }

  private void saveAuthentication(String token) {
    Long userId = authTokenProvider.getUserId(token);

    AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId);
    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authenticatedUser);

    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
  }
}
