package br.com.edmilson.bndes.projects.api.auth;

import br.com.edmilson.bndes.projects.api.auth.dto.AuthResponse;
import br.com.edmilson.bndes.projects.api.auth.dto.LoginRequest;
import br.com.edmilson.bndes.projects.api.auth.dto.RegisterRequest;
import br.com.edmilson.bndes.projects.api.model.User;
import br.com.edmilson.bndes.projects.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public void register(RegisterRequest request) {
    String email = normalizeEmail(request.email());

    if (userRepository.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
    }

    String hash = passwordEncoder.encode(request.password());
    userRepository.save(new User(email, hash));
  }

  public AuthResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

    if (!Boolean.TRUE.equals(user.getEnabled())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is disabled.");
    }

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
    }

    String token = jwtService.generateToken(
        user.getEmail(),
        Map.of("uid", user.getId(), "role", "USER")
    );

    return new AuthResponse(token, "Bearer", jwtService.getExpiresInSeconds());
  }

  private String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase();
  }
}
