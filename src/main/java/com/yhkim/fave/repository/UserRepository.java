package com.yhkim.fave.repository;

import com.yhkim.fave.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByContact(String contact);
    Optional<UserEntity> findByNickname(String nickname);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByContact(String contact);
    boolean existsByNickname(String nickname);
}