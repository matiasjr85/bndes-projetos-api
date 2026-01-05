package br.com.edmilson.bndes.projects.api.auth;

import br.com.edmilson.bndes.projects.api.auth.dto.LoginRequest;
import br.com.edmilson.bndes.projects.api.auth.dto.RefreshRequest;
import br.com.edmilson.bndes.projects.api.auth.dto.RegisterRequest;
import br.com.edmilson.bndes.projects.api.exception.UnauthorizedException;
import br.com.edmilson.bndes.projects.api.model.RefreshToken;
import br.com.edmilson.bndes.projects.api.model.Role;
import br.com.edmilson.bndes.projects.api.model.User;
import br.com.edmilson.bndes.projects.api.repository.RefreshTokenRepository;
import br.com.edmilson.bndes.projects.api.repository.RevokedTokenRepository;
import br.com.edmilson.bndes.projects.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository userRepository;
  @Mock RefreshTokenRepository refreshTokenRepository;
  @Mock RevokedTokenRepository revokedTokenRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtService jwtService;

  AuthService service;

  @BeforeEach
  void setup() {
    service = new AuthService(
        userRepository,
        refreshTokenRepository,
        revokedTokenRepository,
        passwordEncoder,
        jwtService,
        7L
    );
  }

  @Test
  void register_deveCriarUsuario_quandoDadosValidos() {
    RegisterRequest req = new RegisterRequest("User@Test.com", "Test@1234");

    when(userRepository.findByEmailIgnoreCase("user@test.com"))
        .thenReturn(Optional.empty());

    service.register(req);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    User saved = captor.getValue();
    assertThat(saved.getEmail()).isEqualTo("user@test.com");
    assertThat(saved.getRole()).isEqualTo(Role.USER);

    // Não stubba nem verifica encode aqui (evita UnnecessaryStubbingException)
    // Se você quiser, pode garantir que nenhum método do encoder foi chamado:
    // verifyNoInteractions(passwordEncoder);
  }

  @Test
  void register_deveSerIdempotente_quandoEmailJaExiste() {
    RegisterRequest req = new RegisterRequest("user@test.com", "Test@1234");

    User existing = new User();
    existing.setEmail("user@test.com");
    existing.setRole(Role.USER);

    when(userRepository.findByEmailIgnoreCase("user@test.com"))
        .thenReturn(Optional.of(existing));

    service.register(req);

    // comportamento atual: o service chama save mesmo existindo (idempotente / re-save)
    verify(userRepository).save(any(User.class));

    // Não stubba nem verifica encode aqui (evita UnnecessaryStubbingException)
    // verifyNoInteractions(passwordEncoder);
  }

  @Test
  void login_deveRetornarTokenERefresh_quandoCredenciaisValidas() {
    User user = new User();
    user.setId(10L);
    user.setEmail("user@test.com");
    user.setPasswordHash("HASH");
    user.setRole(Role.USER);
    user.setEnabled(true);

    when(userRepository.findByEmailIgnoreCase("user@test.com"))
        .thenReturn(Optional.of(user));
    when(passwordEncoder.matches("Test@1234", "HASH"))
        .thenReturn(true);

    when(jwtService.generateToken(eq("user@test.com"), anyMap()))
        .thenReturn("ACCESS");
    when(jwtService.getExpiresInSeconds())
        .thenReturn(3600L);

    var resp = service.login(new LoginRequest("user@test.com", "Test@1234"));

    assertThat(resp.token()).isEqualTo("ACCESS");
    assertThat(resp.expiresInSeconds()).isEqualTo(3600L);
    assertThat(resp.refreshToken()).isNotBlank();
    assertThat(resp.refreshExpiresInSeconds()).isGreaterThan(0);

    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void login_deveDar401_quandoSenhaInvalida() {
    User user = new User();
    user.setEmail("user@test.com");
    user.setPasswordHash("HASH");
    user.setEnabled(true);

    when(userRepository.findByEmailIgnoreCase("user@test.com"))
        .thenReturn(Optional.of(user));
    when(passwordEncoder.matches("X", "HASH"))
        .thenReturn(false);

    assertThatThrownBy(() -> service.login(new LoginRequest("user@test.com", "X")))
        .isInstanceOf(UnauthorizedException.class);

    verify(jwtService, never()).generateToken(anyString(), anyMap());
  }

  @Test
  void refresh_deveEmitirNovoAccessToken_quandoRefreshValido() {
    User user = new User();
    user.setId(1L);
    user.setEmail("user@test.com");
    user.setRole(Role.USER);
    user.setEnabled(true);

    RefreshToken existing =
        new RefreshToken(user, "HASH", Instant.now().plusSeconds(600));

    when(refreshTokenRepository.findByTokenHash(anyString()))
        .thenReturn(Optional.of(existing));

    when(jwtService.generateToken(eq("user@test.com"), anyMap()))
        .thenReturn("ACCESS2");
    when(jwtService.getExpiresInSeconds())
        .thenReturn(3600L);

    var resp = service.refresh(new RefreshRequest("RAW_REFRESH"));

    assertThat(resp.token()).isEqualTo("ACCESS2");
    assertThat(resp.expiresInSeconds()).isEqualTo(3600L);
    assertThat(resp.refreshToken()).isNotBlank();

    verify(refreshTokenRepository, atLeastOnce()).save(any(RefreshToken.class));
  }

  @Test
  void logout_deveRevogarRefreshEBlackListarJti_quandoBearerPresente() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    var principal = mock(java.security.Principal.class);
    when(principal.getName()).thenReturn("user@test.com");
    when(request.getUserPrincipal()).thenReturn(principal);

    User user = new User();
    user.setId(1L);
    user.setEmail("user@test.com");
    user.setEnabled(true);

    when(userRepository.findByEmailIgnoreCase("user@test.com"))
        .thenReturn(Optional.of(user));

    when(request.getHeader("Authorization"))
        .thenReturn("Bearer ACCESS");
    when(jwtService.extractJti("ACCESS"))
        .thenReturn("JTI-123");
    when(jwtService.extractExpirationInstant("ACCESS"))
        .thenReturn(Instant.now().plusSeconds(300));

    service.logout(request);

    verify(refreshTokenRepository).revokeAllByUserId(1L);
    verify(revokedTokenRepository).save(any());
  }
}
