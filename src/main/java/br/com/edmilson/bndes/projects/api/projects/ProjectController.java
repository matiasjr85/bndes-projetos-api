package br.com.edmilson.bndes.projects.api.projects;

import br.com.edmilson.bndes.projects.api.projects.dto.ProjectCreateRequest;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectResponse;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/projects")
public class ProjectController {

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "id",
      "name",
      "description",
      "value",
      "active",
      "startDate",
      "endDate",
      "createdAt",
      "updatedAt"
  );

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  // RF01
  @Operation(summary = "Create project")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest request) {
    return projectService.create(request);
  }

  // RF02 + RF03 (pagination/sort + filter)
  @Operation(summary = "List projects with pagination, sorting and filters")
  @GetMapping
  public Page<ProjectResponse> list(
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false, defaultValue = "") String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort
  ) {
    // Normaliza entradas básicas (evita erro bobo)
    if (page < 0) page = 0;
    if (size <= 0) size = 10;
    if (size > 100) size = 100; // limite saudável pra API

    String[] sortParts = sort.split(",");
    String field = sortParts[0].trim();

    // ✅ Whitelist de sort field (evita quebrar ou expor campo indevido)
    if (!ALLOWED_SORT_FIELDS.contains(field)) {
      field = "id";
    }

    Sort.Direction dir = (sortParts.length > 1 && sortParts[1].trim().equalsIgnoreCase("asc"))
        ? Sort.Direction.ASC
        : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(dir, field));
    return projectService.list(active, q, pageable);
  }

  // RF04
  @Operation(summary = "Get project by id")
  @GetMapping("/{id}")
  public ProjectResponse getById(@PathVariable Long id) {
    return projectService.getById(id);
  }

  // RF05
  @Operation(summary = "Update project")
  @PutMapping("/{id}")
  public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
    return projectService.update(id, request);
  }

  // RF06 (logical delete)
  @Operation(summary = "Delete project (logical delete)")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    projectService.delete(id);
  }
}
