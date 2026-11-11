package com.mycloud.backend.users.repository;

import com.mycloud.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
