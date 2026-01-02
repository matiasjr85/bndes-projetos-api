package br.com.edmilson.bndes.projects.api.repository;

import br.com.edmilson.bndes.projects.api.model.Project;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  // ✅ LISTAGEM NORMAL (SEM FTS) — respeita sort do Pageable (id,desc etc.)
  @Query("""
      SELECT p
      FROM Project p
      WHERE p.deletedAt IS NULL
        AND LOWER(p.user.email) = LOWER(:email)
        AND (:active IS NULL OR p.active = :active)
      """)
  Page<Project> findAllByUserEmail(
      @Param("email") String email,
      @Param("active") Boolean active,
      Pageable pageable
  );

  // ✅ FTS (NATIVE) — usa ranking + created_at (ordem própria)
  // ⚠️ Recomendo NÃO passar sort do pageable quando usar esse método (ver ajuste no service)
  @Query(
      value = """
        SELECT p.*
        FROM projects p
        JOIN users u ON u.id = p.user_id
        WHERE p.deleted_at IS NULL
          AND LOWER(u.email) = LOWER(:email)
          AND (:active IS NULL OR p.active = :active)
          AND (
            :q IS NULL OR :q = '' OR
            to_tsvector('portuguese', COALESCE(p.name,'') || ' ' || COALESCE(p.description,'')) @@
            plainto_tsquery('portuguese', :q)
          )
        ORDER BY
          CASE WHEN :q IS NULL OR :q = '' THEN 0
              ELSE ts_rank(
                to_tsvector('portuguese', COALESCE(p.name,'') || ' ' || COALESCE(p.description,'')),
                plainto_tsquery('portuguese', :q)
              )
          END DESC,
          p.created_at DESC
      """,
      countQuery = """
        SELECT COUNT(*)
        FROM projects p
        JOIN users u ON u.id = p.user_id
        WHERE p.deleted_at IS NULL
          AND LOWER(u.email) = LOWER(:email)
          AND (:active IS NULL OR p.active = :active)
          AND (
            :q IS NULL OR :q = '' OR
            to_tsvector('portuguese', COALESCE(p.name,'') || ' ' || COALESCE(p.description,'')) @@
            plainto_tsquery('portuguese', :q)
          )
      """,
      nativeQuery = true
  )
  Page<Project> searchByUserEmail(
      @Param("email") String email,
      @Param("active") Boolean active,
      @Param("q") String q,
      Pageable pageable
  );

  // ✅ Buscar projeto ativo por ID (soft delete)
  @Query("""
      SELECT p
      FROM Project p
      WHERE p.id = :id AND p.deletedAt IS NULL
      """)
  Optional<Project> findActiveById(@Param("id") Long id);

  // ✅ Buscar projeto ativo por ID + dono (ownership)
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

    @Query("""
      SELECT p
        FROM Project p
       WHERE p.deletedAt IS NULL
         AND (:active IS NULL OR p.active = :active)
      """)
  Page<Project> findAllActive(
      @Param("active") Boolean active,
      Pageable pageable
  );

  @Query(
      value = """
        SELECT p.*
          FROM projects p
         WHERE p.deleted_at IS NULL
           AND (:active IS NULL OR p.active = :active)
           AND (
             :q IS NULL OR :q = '' OR
             to_tsvector('portuguese', COALESCE(p.name,'') || ' ' || COALESCE(p.description,'')) @@
             plainto_tsquery('portuguese', :q)
           )
         ORDER BY
           CASE WHEN :q IS NULL OR :q = '' THEN 0
                ELSE ts_rank(
                  to_tsvector('portuguese', COALESCE(p.name,'') || ' ' || COALESCE(p.description,'')),
                  plainto_tsquery('portuguese', :q)
                )
           END DESC,
           p.created_at DESC
      """,
      countQuery = """
        SELECT COUNT(*)
          FROM projects p
         WHERE p.deleted_at IS NULL
           AND (:active IS NULL OR p.active = :active)
           AND (
             :q IS NULL OR :q = '' OR
             to_tsvector('portuguese', COALESCE(p.name,'') || ' ' || COALESCE(p.description,'')) @@
             plainto_tsquery('portuguese', :q)
           )
      """,
      nativeQuery = true
  )
  Page<Project> searchAll(
      @Param("active") Boolean active,
      @Param("q") String q,
      Pageable pageable
  );
}
