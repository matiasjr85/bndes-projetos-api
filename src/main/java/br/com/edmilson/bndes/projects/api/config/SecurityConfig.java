package br.com.edmilson.bndes.projects.api.config;

import br.com.edmilson.bndes.projects.api.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final CorsConfigurationSource corsConfigurationSource;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.corsConfigurationSource = corsConfigurationSource;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource))
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(ex -> ex
          .authenticationEntryPoint((req, res, e) -> res.sendError(401, "Unauthorized"))
          .accessDeniedHandler((req, res, e) -> res.sendError(403, "Forbidden"))
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
}

