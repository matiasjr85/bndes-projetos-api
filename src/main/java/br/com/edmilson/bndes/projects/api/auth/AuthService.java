package br.com.edmilson.bndes.projects.api.auth;

import br.com.edmilson.bndes.projects.api.auth.dto.AuthResponse;
import br.com.edmilson.bndes.projects.api.auth.dto.LoginRequest;
import br.com.edmilson.bndes.projects.api.auth.dto.RefreshRequest;
import br.com.edmilson.bndes.projects.api.auth.dto.RegisterRequest;
import br.com.edmilson.bndes.projects.api.exception.UnauthorizedException;
import br.com.edmilson.bndes.projects.api.exception.ValidationException;
import br.com.edmilson.bndes.projects.api.messages.ApiMessages;
import br.com.edmilson.bndes.projects.api.model.RefreshToken;
import br.com.edmilson.bndes.projects.api.model.RevokedToken;
import br.com.edmilson.bndes.projects.api.model.Role;
import br.com.edmilson.bndes.projects.api.model.User;
import br.com.edmilson.bndes.projects.api.repository.RefreshTokenRepository;
import br.com.edmilson.bndes.projects.api.repository.RevokedTokenRepository;
import br.com.edmilson.bndes.projects.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final RevokedTokenRepository revokedTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  private final long refreshExpirationDays;

  public AuthService(
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      RevokedTokenRepository revokedTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      @Value("${app.jwt.refreshExpirationDays:7}") long refreshExpirationDays
  ) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.revokedTokenRepository = revokedTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.refreshExpirationDays = refreshExpirationDays;
  }

  @Transactional
  public void register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    if (email.isBlank()) throw new ValidationException(ApiMessages.VALIDATION_ERROR);

    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new ValidationException(ApiMessages.USER_ALREADY_EXISTS);
    }

    String hash = passwordEncoder.encode(request.password());

    User u = new User(email, hash);
    u.setRole(Role.USER);
    userRepository.save(u);
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    User user = authenticate(request.email(), request.password());

    String accessToken = jwtService.generateToken(
        user.getEmail(),
        Map.of("uid", user.getId(), "role", user.getRole().name())
    );

    RefreshTokenPair refreshPair = issueRefreshToken(user);

    return new AuthResponse(
        accessToken,
        "Bearer",
        jwtService.getExpiresInSeconds(),
        refreshPair.rawToken(),
        refreshPair.expiresInSeconds()
    );
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    String raw = (request == null) ? null : request.refreshToken();
    if (raw == null || raw.isBlank()) {
      throw new UnauthorizedException(ApiMessages.INVALID_REFRESH_TOKEN);
    }

    String hash = sha256Hex(raw);

    RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
        .orElseThrow(() -> new UnauthorizedException(ApiMessages.INVALID_REFRESH_TOKEN));

    if (!existing.isActive()) {
      throw new UnauthorizedException(ApiMessages.INVALID_REFRESH_TOKEN);
    }

    // ✅ Rotação: revoga o atual
    existing.setRevokedAt(Instant.now());
    refreshTokenRepository.save(existing);

    User user = existing.getUser();

    String accessToken = jwtService.generateToken(
        user.getEmail(),
        Map.of("uid", user.getId(), "role", user.getRole().name())
    );

    RefreshTokenPair newRefresh = issueRefreshToken(user);

    return new AuthResponse(
        accessToken,
        "Bearer",
        jwtService.getExpiresInSeconds(),
        newRefresh.rawToken(),
        newRefresh.expiresInSeconds()
    );
  }

  @Transactional
  public void logout(HttpServletRequest request) {
    // ✅ revoga todos refresh tokens do usuário
    User user = getCurrentUserFromRequest(request);
    refreshTokenRepository.revokeAllByUserId(user.getId());

    // ✅ blacklist do access token atual (JTI)
    String auth = request.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) return;

    String token = auth.substring(7).trim();

    String jti = jwtService.extractJti(token);
    Instant exp = jwtService.extractExpirationInstant(token);

    if (jti == null || jti.isBlank() || exp == null) return;

    RevokedToken rt = new RevokedToken(jti, user, exp);

    try {
      revokedTokenRepository.save(rt);
    } catch (DataIntegrityViolationException ignored) {
      // já está revogado (idempotente)
    }
  }

  private User authenticate(String emailRaw, String passwordRaw) {
    String email = normalizeEmail(emailRaw);

    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new UnauthorizedException(ApiMessages.INVALID_CREDENTIALS));

    if (!Boolean.TRUE.equals(user.getEnabled())) {
      throw new UnauthorizedException(ApiMessages.USER_DISABLED);
    }

    if (!passwordEncoder.matches(passwordRaw, user.getPasswordHash())) {
      throw new UnauthorizedException(ApiMessages.INVALID_CREDENTIALS);
    }

    if (user.getRole() == null) user.setRole(Role.USER);

    return user;
  }

  private User getCurrentUserFromRequest(HttpServletRequest request) {
    // SecurityContext já vai estar populado pelo JwtAuthFilter
    String email = (request.getUserPrincipal() != null) ? request.getUserPrincipal().getName() : null;
    if (email == null || email.isBlank()) {
      throw new UnauthorizedException(ApiMessages.UNAUTHORIZED);
    }

    return userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new UnauthorizedException(ApiMessages.UNAUTHORIZED));
  }

  private RefreshTokenPair issueRefreshToken(User user) {
    String raw = generateSecureToken();
    String hash = sha256Hex(raw);

    Instant exp = Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS);

    RefreshToken entity = new RefreshToken(user, hash, exp);
    refreshTokenRepository.save(entity);

    long expiresInSeconds = refreshExpirationDays * 24L * 60L * 60L;
    return new RefreshTokenPair(raw, expiresInSeconds);
  }

  private String generateSecureToken() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String sha256Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to hash token.", e);
    }
  }

  private String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase();
  }

  private record RefreshTokenPair(String rawToken, long expiresInSeconds) {}
}
