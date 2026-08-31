package org.jejuro.miraero.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthTokenProvider implements AuthTokenProvider {

  private static final String TOKEN_TYPE_CLAIM = "tokenType";
  private static final String ACCESS_TOKEN_TYPE = "ACCESS";
  private static final String REFRESH_TOKEN_TYPE = "REFRESH";
  private static final int MILLISECONDS_PER_SECOND = 1000;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token-expires-in}")
  private Long accessTokenExpiresIn;

  @Value("${jwt.refresh-token-expires-in}")
  private Long refreshTokenExpiresIn;

  private Key signingKey;

  @PostConstruct
  public void init() {
    signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String createAccessToken(Long userId) {
    return createToken(userId, ACCESS_TOKEN_TYPE, accessTokenExpiresIn);
  }

  @Override
  public String createRefreshToken(Long userId) {
    return createToken(userId, REFRESH_TOKEN_TYPE, refreshTokenExpiresIn);
  }

  @Override
  public Long getAccessTokenExpiresIn() {
    return accessTokenExpiresIn;
  }

  @Override
  public Long getRefreshTokenExpiresIn() {
    return refreshTokenExpiresIn;
  }

  @Override
  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException exception) {
      return false;
    }
  }

  @Override
  public Long getUserId(String token) {
    return Long.valueOf(parseClaims(token).getSubject());
  }

  @Override
  public boolean isAccessToken(String token) {
    String tokenType = parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class);

    return ACCESS_TOKEN_TYPE.equals(tokenType);
  }

  @Override
  public boolean isRefreshToken(String token) {
    String tokenType = parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class);

    return REFRESH_TOKEN_TYPE.equals(tokenType);
  }

  private String createToken(
      Long userId,
      String tokenType,
      Long expiresIn
  ) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expiresIn * MILLISECONDS_PER_SECOND);

    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .claim(TOKEN_TYPE_CLAIM, tokenType)
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
