package com.project.shopapp.repositories;

import com.project.shopapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findByFacebookAccountId(String facebookAccountId);
    Optional<User> findByGoogleAccountId(String googleAccountId);
}
