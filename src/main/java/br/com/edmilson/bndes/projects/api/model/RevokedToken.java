package br.com.edmilson.bndes.projects.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

  @Id
  @Column(name = "jti", length = 64)
  private String jti;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "revoked_at", nullable = false)
  private Instant revokedAt = Instant.now();

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  public RevokedToken() {}

  public RevokedToken(String jti, User user, Instant expiresAt) {
    this.jti = jti;
    this.user = user;
    this.expiresAt = expiresAt;
    this.revokedAt = Instant.now();
  }

  public String getJti() { return jti; }
  public void setJti(String jti) { this.jti = jti; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public Instant getRevokedAt() { return revokedAt; }
  public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

  public Instant getExpiresAt() { return expiresAt; }
  public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
