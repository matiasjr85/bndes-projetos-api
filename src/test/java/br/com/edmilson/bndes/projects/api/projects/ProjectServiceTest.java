package br.com.edmilson.bndes.projects.api.projects;

import br.com.edmilson.bndes.projects.api.exception.ForbiddenException;
import br.com.edmilson.bndes.projects.api.model.Project;
import br.com.edmilson.bndes.projects.api.model.Role;
import br.com.edmilson.bndes.projects.api.model.User;
import br.com.edmilson.bndes.projects.api.projects.dto.ProjectCreateRequest;
import br.com.edmilson.bndes.projects.api.repository.ProjectRepository;
import br.com.edmilson.bndes.projects.api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock ProjectRepository projectRepository;
  @Mock UserRepository userRepository;

  ProjectService service;

  @BeforeEach
  void setup() {
    service = new ProjectService(projectRepository, userRepository);
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  private void authAs(String email) {
    SecurityContextHolder.getContext().setAuthentication(
        new TestingAuthenticationToken(email, "N/A", "ROLE_USER")
    );
  }

  @Test
  void list_deveListarSomenteDoUsuario_quandoNaoAdminESemBusca() {
    authAs("user@test.com");

    User user = new User();
    user.setEmail("user@test.com");
    user.setRole(Role.USER);
    user.setEnabled(true);

    when(userRepository.findByEmailIgnoreCase("user@test.com"))
        .thenReturn(Optional.of(user));

    Page<Project> page = new PageImpl<>(
        java.util.List.of(new Project()),
        PageRequest.of(0, 10),
        1
    );

    when(projectRepository.findAllByUserEmail(eq("user@test.com"), eq(true), any(Pageable.class)))
        .thenReturn(page);

    var resp = service.list(true, "", PageRequest.of(0, 10));

    assertThat(resp.getTotalElements()).isEqualTo(1);
    verify(projectRepository).findAllByUserEmail(eq("user@test.com"), eq(true), any(Pageable.class));
  }

  @Test
  void create_deveSalvarProjeto_comCamposObrigatorios() {
    authAs("user@test.com");

    User user = new User();
    user.setId(1L);
    user.setEmail("user@test.com");
    user.setRole(Role.USER);
    user.setEnabled(true);

    when(userRepository.findByEmailIgnoreCase("user@test.com"))
        .thenReturn(Optional.of(user));

    when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
      Project p = inv.getArgument(0);
      p.setId(99L);
      return p;
    });

    ProjectCreateRequest req = new ProjectCreateRequest(
        "Projeto",
        "Desc",
        new BigDecimal("150000.00"),
        true,
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2025, 12, 31)
    );

    var resp = service.create(req);

    assertThat(resp.id()).isEqualTo(99L);
    verify(projectRepository).save(any(Project.class));
  }

  @Test
  void getById_deveNegarQuandoNaoDonoENaoAdmin() {
    authAs("user2@test.com");

    User user2 = new User();
    user2.setEmail("user2@test.com");
    user2.setRole(Role.USER);
    user2.setEnabled(true);

    when(userRepository.findByEmailIgnoreCase("user2@test.com"))
        .thenReturn(Optional.of(user2));
    
    Project p = new Project();
    User owner = new User();
    owner.setEmail("user1@test.com");
    p.setUser(owner);
    
    when(projectRepository.findActiveByIdAndUserEmail(eq(1L), eq("user2@test.com")))
        .thenReturn(Optional.empty());
    
    lenient().when(projectRepository.findActiveById(1L))
        .thenReturn(Optional.of(p));

    assertThatThrownBy(() -> service.getById(1L))
        .isInstanceOf(ForbiddenException.class);
  }
}
