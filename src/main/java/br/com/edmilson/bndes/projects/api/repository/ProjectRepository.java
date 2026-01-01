package br.com.edmilson.bndes.projects.api.repository;

import br.com.edmilson.bndes.projects.api.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  /**
   * Lista SOMENTE projetos do usuário logado, com filtro opcional por active e busca por texto (name/description).
   */
  @Query("""
      SELECT p
      FROM Project p
      WHERE p.deletedAt IS NULL
        AND LOWER(p.user.email) = LOWER(:email)
        AND (:active IS NULL OR p.active = :active)
        AND (
          :q IS NULL OR :q = '' OR
          LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
          LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))
        )
      ORDER BY p.createdAt DESC
      """)
  Page<Project> searchByUserEmail(
      @Param("email") String email,
      @Param("active") Boolean active,
      @Param("q") String q,
      Pageable pageable
  );

  /**
   * Busca projeto ativo (não deletado) pelo id, garantindo que pertence ao usuário logado.
   */
  @Query("""
      SELECT p
      FROM Project p
      WHERE p.id = :id
        AND p.deletedAt IS NULL
        AND LOWER(p.user.email) = LOWER(:email)
      """)
  Optional<Project> findActiveByIdAndUserEmail(
      @Param("id") Long id,
      @Param("email") String email
  );

  /**
   * (Opcional) caso você ainda use em algum lugar sem filtro de usuário.
   */
  @Query("""
      SELECT p
      FROM Project p
      WHERE p.id = :id AND p.deletedAt IS NULL
      """)
  Optional<Project> findActiveById(@Param("id") Long id);
}
