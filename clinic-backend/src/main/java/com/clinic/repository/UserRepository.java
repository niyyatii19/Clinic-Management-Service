package com.clinic.repository;

import com.clinic.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String name);
    boolean existsByEmail(String email);
    boolean existsByUserName(String name);
}
