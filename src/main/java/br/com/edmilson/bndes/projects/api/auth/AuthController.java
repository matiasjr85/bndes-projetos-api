package br.com.edmilson.bndes.projects.api.auth;

import br.com.edmilson.bndes.projects.api.auth.dto.AuthResponse;
import br.com.edmilson.bndes.projects.api.auth.dto.LoginRequest;
import br.com.edmilson.bndes.projects.api.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
  
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;    
  }

  @Operation(summary = "Register new user")
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public void register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);
  }

  @Operation(summary = "Login (returns JWT)")
  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {    
    return authService.login(request);
  }

  @Operation(summary = "Logout (client-side) - just clear the token on frontend")
  @PostMapping("/logout")
  public void logout() {    
  }  
}
