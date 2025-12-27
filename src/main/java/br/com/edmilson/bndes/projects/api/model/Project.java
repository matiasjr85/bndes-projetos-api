package br.com.edmilson.bndes.projects.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "projects",
    indexes = {
        @Index(name = "idx_projects_active", columnList = "active"),
        @Index(name = "idx_projects_deleted_at", columnList = "deleted_at")
    }
)
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal value;

  @Column(nullable = false)
  private Boolean active = true; // true=ativo, false=inativo

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public Project() {}

  @PrePersist
  void prePersist() {
    if (this.createdAt == null) this.createdAt = Instant.now();
    if (this.active == null) this.active = true;
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = Instant.now();
  }

  public void softDelete() {
    this.deletedAt = Instant.now();
    this.active = false;
  }

  public boolean isDeleted() { return deletedAt != null; }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public BigDecimal getValue() { return value; }
  public void setValue(BigDecimal value) { this.value = value; }

  public Boolean getActive() { return active; }
  public void setActive(Boolean active) { this.active = active; }

  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

  public Instant getDeletedAt() { return deletedAt; }
  public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
