package br.com.edmilson.bndes.projects.api.repository;

import br.com.edmilson.bndes.projects.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  // (Opcional) se você ainda usa os métodos antigos em algum lugar:
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
