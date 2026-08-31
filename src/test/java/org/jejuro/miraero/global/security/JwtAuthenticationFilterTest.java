package org.jejuro.miraero.global.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private final AuthTokenProvider authTokenProvider =
      Mockito.mock(AuthTokenProvider.class);

  private final JwtAuthenticationFilter filter =
      new JwtAuthenticationFilter(authTokenProvider);

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Authorization 헤더가 없으면 인증 정보를 저장하지 않는다")
  void doFilter_noAuthorizationHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = Mockito.mock(FilterChain.class);

    filter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("유효한 Access Token이면 인증 정보를 저장한다")
  void doFilter_validAccessToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = Mockito.mock(FilterChain.class);

    request.addHeader("Authorization", "Bearer access-token");

    when(authTokenProvider.validateToken("access-token"))
        .thenReturn(true);
    when(authTokenProvider.isAccessToken("access-token"))
        .thenReturn(true);
    when(authTokenProvider.getUserId("access-token"))
        .thenReturn(1L);

    filter.doFilter(request, response, filterChain);

    Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();
    AuthenticatedUser user =
        (AuthenticatedUser) authentication.getPrincipal();

    assertEquals(1L, user.getUserId());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Refresh Token이면 인증 정보를 저장하지 않는다")
  void doFilter_refreshToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = Mockito.mock(FilterChain.class);

    request.addHeader("Authorization", "Bearer refresh-token");

    when(authTokenProvider.validateToken("refresh-token"))
        .thenReturn(true);
    when(authTokenProvider.isAccessToken("refresh-token"))
        .thenReturn(false);

    filter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("잘못된 토큰이면 인증 정보를 저장하지 않는다")
  void doFilter_invalidToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = Mockito.mock(FilterChain.class);

    request.addHeader("Authorization", "Bearer invalid-token");

    when(authTokenProvider.validateToken("invalid-token"))
        .thenReturn(false);

    filter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }
}
