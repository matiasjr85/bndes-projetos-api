package br.com.edmilson.bndes.projects.api.repository;

import br.com.edmilson.bndes.projects.api.model.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  @Modifying
  @Query("""
      UPDATE RefreshToken rt
         SET rt.revokedAt = CURRENT_TIMESTAMP
       WHERE rt.user.id = :userId
         AND rt.revokedAt IS NULL
      """)
  int revokeAllByUserId(@Param("userId") Long userId);
}
