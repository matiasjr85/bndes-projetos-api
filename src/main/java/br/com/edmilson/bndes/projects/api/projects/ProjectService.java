package br.com.edmilson.bndes.projects.api.projects;

import br.com.edmilson.bndes.projects.api.exception.ForbiddenException;
import br.com.edmilson.bndes.projects.api.exception.ResourceNotFoundException;
import br.com.edmilson.bndes.projects.api.exception.UnauthorizedException;
import br.com.edmilson.bndes.projects.api.exception.ValidationException;
import br.com.edmilson.bndes.projects.api.messages.ApiMessages;
import br.com.edmilson.bndes.projects.api.model.Project;
import br.com.edmilson.bndes.projects.api.model.User;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectCreateRequest;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectResponse;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectUpdateRequest;
import br.com.edmilson.bndes.projects.api.repository.ProjectRepository;
import br.com.edmilson.bndes.projects.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;

  public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
  }

  public ProjectResponse create(ProjectCreateRequest req) {
    User currentUser = getCurrentUser();

    Project p = new Project();
    p.setUser(currentUser); // ✅ dono do projeto
    applyCreate(p, req);
    validateDates(p.getStartDate(), p.getEndDate());

    Project saved = projectRepository.save(p);
    return toResponse(saved);
  }

  /**
   * ✅ Lista SOMENTE projetos do usuário logado
   * - Sem busca (q vazio): JPQL (respeita sort do pageable)
   * - Com busca (q preenchido): FTS native (remove sort para evitar ORDER BY ambíguo)
   */
  public Page<ProjectResponse> list(Boolean active, String q, Pageable pageable) {
    User currentUser = getCurrentUser();
    boolean hasQuery = (q != null && !q.isBlank());

    boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().name().equals("ADMIN");

    if (!hasQuery) {
      if (isAdmin) {
        return projectRepository.findAllActive(active, pageable).map(this::toResponse);
      }
      return projectRepository.findAllByUserEmail(currentUser.getEmail(), active, pageable)
          .map(this::toResponse);
    }

    Pageable noSort = pageable.isUnpaged()
        ? Pageable.unpaged()
        : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

    if (isAdmin) {
      return projectRepository.searchAll(active, q.trim(), noSort).map(this::toResponse);
    }

    return projectRepository.searchByUserEmail(currentUser.getEmail(), active, q.trim(), noSort)
        .map(this::toResponse);
  }

  public ProjectResponse getById(Long id) {
    String email = getCurrentUser().getEmail();
    Project p = getOwnedOrThrow(id, email);
    return toResponse(p);
  }

  public ProjectResponse update(Long id, ProjectUpdateRequest req) {
    String email = getCurrentUser().getEmail();
    Project p = getOwnedOrThrow(id, email);

    applyUpdate(p, req);
    validateDates(p.getStartDate(), p.getEndDate());

    return toResponse(projectRepository.save(p));
  }

  // RF06 — logical delete
  public void delete(Long id) {
    String email = getCurrentUser().getEmail();
    Project p = getOwnedOrThrow(id, email);

    p.softDelete();
    projectRepository.save(p);
  }

  /**
   * ✅ Busca o projeto do usuário logado.
   * - Se existir e for do usuário: retorna
   * - Se existir, mas for de outro usuário: 403
   * - Se não existir (ou deletado): 404
   */
  private Project getOwnedOrThrow(Long id, String email) {
    User currentUser = getCurrentUser();
    boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().name().equals("ADMIN");

    if (isAdmin) {
      return projectRepository.findActiveById(id)
          .orElseThrow(() -> new ResourceNotFoundException(ApiMessages.PROJECT_NOT_FOUND));
    }

    Optional<Project> owned = projectRepository.findActiveByIdAndUserEmail(id, email);
    if (owned.isPresent()) return owned.get();

    Optional<Project> exists = projectRepository.findActiveById(id);
    if (exists.isEmpty()) {
      throw new ResourceNotFoundException(ApiMessages.PROJECT_NOT_FOUND);
    }
    throw new ForbiddenException(ApiMessages.PROJECT_ACCESS_FORBIDDEN);
  }

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getName() == null || auth.getName().isBlank()) {
      throw new UnauthorizedException(ApiMessages.UNAUTHORIZED);
    }

    String email = auth.getName();
    return userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new UnauthorizedException(ApiMessages.UNAUTHORIZED));
  }

  private void applyCreate(Project p, ProjectCreateRequest req) {
    Boolean active = (req.active() != null) ? req.active() : Boolean.TRUE;

    p.setName(req.name());
    p.setDescription(req.description());
    p.setValue(req.value());
    p.setActive(active);
    p.setStartDate(req.startDate());
    p.setEndDate(req.endDate());
  }

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

  private void validateDates(LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new ValidationException("endDate must be greater than or equal to startDate.");
    }
  }
}
