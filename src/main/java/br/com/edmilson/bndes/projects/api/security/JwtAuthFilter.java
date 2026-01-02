package br.com.edmilson.bndes.projects.api.security;

import br.com.edmilson.bndes.projects.api.auth.JwtService;
import br.com.edmilson.bndes.projects.api.exception.ApiError;
import br.com.edmilson.bndes.projects.api.messages.ApiMessages;
import br.com.edmilson.bndes.projects.api.repository.RevokedTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;
  private final RevokedTokenRepository revokedTokenRepository;
  private final ObjectMapper objectMapper;

  public JwtAuthFilter(
      JwtService jwtService,
      CustomUserDetailsService userDetailsService,
      RevokedTokenRepository revokedTokenRepository,
      ObjectMapper objectMapper
  ) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.revokedTokenRepository = revokedTokenRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    if (path == null) return false;

    // ✅ logout PRECISA passar pelo filtro para autenticar e revogar
    if (path.equals("/auth/logout")) return false;

    return path.equals("/auth/login")
        || path.equals("/auth/register")
        || path.equals("/auth/refresh")
        || path.equals("/health")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.equals("/swagger-ui.html");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7).trim();

    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Claims claims = jwtService.parseAllClaims(token);

      String email = claims.getSubject();
      if (email == null || email.isBlank()) {
        writeUnauthorized(response, request, ApiMessages.INVALID_TOKEN, "invalid_token");
        return;
      }

      String jti = claims.get("jti", String.class);
      if (jti != null && revokedTokenRepository.existsByJti(jti)) {
        writeUnauthorized(response, request, ApiMessages.TOKEN_REVOKED, "token_revoked");
        return;
      }

      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);

      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException ex) {
      writeUnauthorized(response, request, ApiMessages.TOKEN_EXPIRED, "token_expired");

    } catch (JwtException | IllegalArgumentException ex) {
      writeUnauthorized(response, request, ApiMessages.INVALID_TOKEN, "invalid_token");
    }
  }

  private void writeUnauthorized(
      HttpServletResponse response,
      HttpServletRequest request,
      String message,
      String errorCode
  ) throws IOException {

    if (response.isCommitted()) return;

    HttpStatus status = HttpStatus.UNAUTHORIZED;

    response.setHeader(
        "WWW-Authenticate",
        "Bearer error=\"" + errorCode + "\", error_description=\"" + message + "\""
    );

    ApiError body = new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        Map.of("code", errorCode)
    );

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), body);
  }
}
