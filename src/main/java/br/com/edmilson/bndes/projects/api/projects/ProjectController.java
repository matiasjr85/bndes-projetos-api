package br.com.edmilson.bndes.projects.api.projects;

import br.com.edmilson.bndes.projects.api.exception.ApiError;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectCreateRequest;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectResponse;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
@Tag(name = "Projects", description = "Endpoints protegidos para gestão de projetos")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Operation(summary = "Create project")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Created"),
      @ApiResponse(responseCode = "400", description = "Validation error",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest request) {
    return projectService.create(request);
  }

  @Operation(summary = "List projects with pagination, sorting and filters")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "401", description = "Unauthorized",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping
  public Page<ProjectResponse> list(
      @RequestParam(required = false) Boolean active,
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
    return projectService.list(active, q, pageable);
  }

  @Operation(summary = "Get project by id")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "401", description = "Unauthorized",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "404", description = "Not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}")
  public ProjectResponse getById(@PathVariable Long id) {
    return projectService.getById(id);
  }

  @Operation(summary = "Update project")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "Validation error",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "404", description = "Not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping("/{id}")
  public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
    return projectService.update(id, request);
  }

  @Operation(summary = "Delete project (logical delete)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "No Content"),
      @ApiResponse(responseCode = "401", description = "Unauthorized",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "404", description = "Not found",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    projectService.delete(id);
  }
}
