package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsById(Long id);
    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByEmail(String email);
}
