package br.com.edmilson.bndes.projects.api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long expirationMinutes;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expirationMinutes}") long expirationMinutes
  ) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMinutes = expirationMinutes;
  }

  public String generateToken(String subject, Map<String, Object> extraClaims) {
    Instant now = Instant.now();
    Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);

    return Jwts.builder()
        .subject(subject)
        .claims(extraClaims)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(secretKey)
        .compact();
  }

  public boolean isTokenValid(String token) {
    try {
      parseAllClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String extractSubject(String token) {
    return parseAllClaims(token).getSubject();
  }

  public long getExpiresInSeconds() {
    return expirationMinutes * 60;
  }

  private Claims parseAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
