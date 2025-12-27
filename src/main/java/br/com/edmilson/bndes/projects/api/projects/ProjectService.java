package br.com.edmilson.bndes.projects.api.projects;

import br.com.edmilson.bndes.projects.api.model.Project;
import br.com.edmilson.bndes.projects.api.projects.dto.*;
import br.com.edmilson.bndes.projects.api.repository.ProjectRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;

  public ProjectService(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  public ProjectResponse create(ProjectCreateRequest req) {
    Project p = new Project();
    p.setName(req.name());
    p.setDescription(req.description());
    p.setValue(req.value());
    p.setActive(req.active());
    p.setStartDate(req.startDate());
    p.setEndDate(req.endDate());
    p.setCreatedAt(Instant.now());

    Project saved = projectRepository.save(p);
    return toResponse(saved);
  }

  public Page<ProjectResponse> list(Boolean active, String q, Pageable pageable) {
    return projectRepository.search(active, q, pageable).map(this::toResponse);
  }

  public ProjectResponse getById(Long id) {
    Project p = projectRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
    return toResponse(p);
  }

  public ProjectResponse update(Long id, ProjectUpdateRequest req) {
    Project p = projectRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));

    p.setName(req.name());
    p.setDescription(req.description());
    p.setValue(req.value());
    p.setActive(req.active());
    p.setStartDate(req.startDate());
    p.setEndDate(req.endDate());
    p.setUpdatedAt(Instant.now());

    return toResponse(projectRepository.save(p));
  }

  // RF06 — logical delete
  public void delete(Long id) {
    Project p = projectRepository.findActiveById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
    p.setDeletedAt(Instant.now());
    projectRepository.save(p);
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
}
