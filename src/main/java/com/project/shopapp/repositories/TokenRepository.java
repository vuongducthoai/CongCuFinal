package com.project.shopapp.repositories;

import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByUser(User user);
    Optional<Token> findByToken(String token);
}
