package br.com.edmilson.bndes.projects.api.repository;

import br.com.edmilson.bndes.projects.api.model.Project;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Query("""
      SELECT p
      FROM Project p
      WHERE p.deletedAt IS NULL
        AND (:status IS NULL OR p.status = :status)
        AND (
          :q IS NULL OR :q = '' OR
          LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
          LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))
        )
      """)
  Page<Project> search(
      @Param("status") Boolean status,
      @Param("q") String q,
      Pageable pageable
  );

  @Query("""
      SELECT p
      FROM Project p
      WHERE p.id = :id AND p.deletedAt IS NULL
      """)
  java.util.Optional<Project> findActiveById(@Param("id") Long id);
}
