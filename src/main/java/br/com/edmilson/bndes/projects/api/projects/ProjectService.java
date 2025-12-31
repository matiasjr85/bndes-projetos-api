package br.com.edmilson.bndes.projects.api.projects;

import br.com.edmilson.bndes.projects.api.model.Project;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectCreateRequest;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectResponse;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectUpdateRequest;
import br.com.edmilson.bndes.projects.api.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;

  public ProjectService(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  public ProjectResponse create(ProjectCreateRequest req) {
    Project p = new Project();
    applyCreate(p, req);
    validateDates(p.getStartDate(), p.getEndDate());
    Project saved = projectRepository.save(p);
    return toResponse(saved);
  }

  public Page<ProjectResponse> list(Boolean active, String q, Pageable pageable) {
    return projectRepository.search(active, q, pageable).map(this::toResponse);
  }

  public ProjectResponse getById(Long id) {
    return toResponse(getOr404(id));
  }

  public ProjectResponse update(Long id, ProjectUpdateRequest req) {
    Project p = getOr404(id);
    applyUpdate(p, req);
    validateDates(p.getStartDate(), p.getEndDate());
    return toResponse(projectRepository.save(p));
  }

  // RF06 — logical delete
  public void delete(Long id) {
    Project p = getOr404(id);
    p.softDelete();
    projectRepository.save(p);
  }

  private Project getOr404(Long id) {
    return projectRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
  }

  /**
   * CREATE: campos obrigatórios chegam preenchidos pelo DTO.
   * active: se vier null, default = true.
   * Timestamps: ficam por conta do @PrePersist/@PreUpdate na Entity.
   */
  private void applyCreate(Project p, ProjectCreateRequest req) {
    Boolean active = (req.active() != null) ? req.active() : Boolean.TRUE;

    p.setName(req.name());
    p.setDescription(req.description());
    p.setValue(req.value());
    p.setActive(active);
    p.setStartDate(req.startDate());
    p.setEndDate(req.endDate());
  }

  /**
   * UPDATE PARCIAL: só altera o que veio no payload.
   * Se um campo vier null, mantém o valor atual (não "zera").
   */
  private void applyUpdate(Project p, ProjectUpdateRequest req) {
    if (req.name() != null && !req.name().isBlank()) {
      p.setName(req.name());
    }
    if (req.description() != null && !req.description().isBlank()) {
      p.setDescription(req.description());
    }
    if (req.value() != null) {
      p.setValue(req.value());
    }
    if (req.active() != null) {
      p.setActive(req.active());
    }
    if (req.startDate() != null) {
      p.setStartDate(req.startDate());
    }
    if (req.endDate() != null) {
      p.setEndDate(req.endDate());
    }
  }

  private ProjectResponse toResponse(Project p) {
    return new ProjectResponse(
        p.getId(),
        p.getName(),
        p.getDescription(),
        p.getValue(),
        p.getActive(),
        p.getStartDate(),
        p.getEndDate(),
        p.getCreatedAt(),
        p.getUpdatedAt()
    );
  }

  private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
  if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
    throw new org.springframework.web.server.ResponseStatusException(
        org.springframework.http.HttpStatus.BAD_REQUEST,
        "endDate must be greater than or equal to startDate."
    );
  }
}

}
