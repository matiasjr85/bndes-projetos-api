package br.com.edmilson.bndes.projects.api.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    },
    indexes = {
        @Index(name = "idx_users_email", columnList = "email")
    }
)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false)
  private Boolean enabled = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  // ✅ Opcional: relacionamento reverso
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Project> projects = new ArrayList<>();

  public User() {}

  public User(String email, String passwordHash) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.enabled = true;
  }

  @PrePersist
  void prePersist() {
    if (this.createdAt == null) this.createdAt = Instant.now();
    if (this.enabled == null) this.enabled = true;
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

  public Boolean getEnabled() { return enabled; }
  public void setEnabled(Boolean enabled) { this.enabled = enabled; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

  public List<Project> getProjects() { return projects; }
  public void setProjects(List<Project> projects) { this.projects = projects; }
}
