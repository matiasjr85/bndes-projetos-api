package br.com.edmilson.bndes.projects.api.config;

import br.com.edmilson.bndes.projects.api.exception.ApiError;
import br.com.edmilson.bndes.projects.api.messages.ApiMessages;
import br.com.edmilson.bndes.projects.api.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final CorsConfigurationSource corsConfigurationSource;
  private final ObjectMapper objectMapper;

  public SecurityConfig(
      JwtAuthFilter jwtAuthFilter,
      CorsConfigurationSource corsConfigurationSource,
      ObjectMapper objectMapper
  ) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.corsConfigurationSource = corsConfigurationSource;
    this.objectMapper = objectMapper;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource))
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((req, res, e) ->
          writeApiError(req.getRequestURI(), res, HttpStatus.UNAUTHORIZED, ApiMessages.UNAUTHORIZED)
        )
        .accessDeniedHandler((req, res, e) ->
          writeApiError(req.getRequestURI(), res, HttpStatus.FORBIDDEN, ApiMessages.ACCESS_DENIED)
        )
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/health").permitAll()
        .requestMatchers("/auth/**").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
      .build();
  }

  private void writeApiError(String path, jakarta.servlet.http.HttpServletResponse res, HttpStatus status, String message) {
    try {
      res.setStatus(status.value());
      res.setContentType(MediaType.APPLICATION_JSON_VALUE);
      res.setCharacterEncoding("UTF-8");

      ApiError body = new ApiError(
          Instant.now(),
          status.value(),
          status.getReasonPhrase(),
          message,
          path,
          null
      );

      objectMapper.writeValue(res.getOutputStream(), body);
    } catch (Exception ignored) {
      // fallback extremo: se falhar serialização, ao menos garante o status
      res.setStatus(status.value());
    }
  }
}
