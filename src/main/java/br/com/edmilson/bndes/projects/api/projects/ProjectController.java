package br.com.edmilson.bndes.projects.api.projects;

import br.com.edmilson.bndes.projects.api.projects.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

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
      @RequestParam(required = false) Boolean status,
      @RequestParam(required = false, defaultValue = "") String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id,desc") String sort
  ) {
    String[] sortParts = sort.split(",");
    String field = sortParts[0];
    Sort.Direction dir = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc"))
        ? Sort.Direction.ASC
        : Sort.Direction.DESC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(dir, field));
    return projectService.list(status, q, pageable);
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
