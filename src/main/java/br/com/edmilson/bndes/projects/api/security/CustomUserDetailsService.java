package br.com.edmilson.bndes.projects.api.security;

import br.com.edmilson.bndes.projects.api.model.User;
import br.com.edmilson.bndes.projects.api.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmailIgnoreCase(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    String roleName = (user.getRole() == null) ? "USER" : user.getRole().name();

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPasswordHash(),
        Boolean.TRUE.equals(user.getEnabled()),
        true,
        true,
        true,
        List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
    );
  }
}
