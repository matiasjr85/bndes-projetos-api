package br.com.edmilson.bndes.projects.api.security;

import br.com.edmilson.bndes.projects.api.auth.JwtService;
import br.com.edmilson.bndes.projects.api.exception.ApiError;
import br.com.edmilson.bndes.projects.api.messages.ApiMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final ObjectMapper objectMapper;

  public JwtAuthFilter(
      JwtService jwtService,
      CustomUserDetailsService userDetailsService,
      ObjectMapper objectMapper
  ) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    if (path == null) return false;

    return path.startsWith("/auth/")
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

    // Sem Bearer -> deixa seguir (SecurityConfig decide se é público/privado)
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7).trim();

    // Se já tem auth no contexto, segue
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Vai lançar ExpiredJwtException se expirado
      String email = jwtService.extractSubject(token);

      if (email == null || email.isBlank()) {
        writeUnauthorized(response, request, ApiMessages.INVALID_TOKEN, "invalid_token");
        return;
      }

      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      // Validação extra (assinatura/malformado etc)
      if (!jwtService.isTokenValid(token)) {
        writeUnauthorized(response, request, ApiMessages.INVALID_TOKEN, "invalid_token");
        return;
      }

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
