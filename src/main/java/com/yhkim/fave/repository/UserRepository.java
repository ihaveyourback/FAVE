package com.yhkim.fave.repository;

import com.yhkim.fave.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByContact(String contact);
    Optional<UserEntity> findByNickname(String nickname);
    Optional<UserEntity> findByEmail(String email);
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.suspended = true")
    boolean existsByIsSuspended();
    boolean existsByEmail(String email);
    boolean existsByContact(String contact);
    boolean existsByNickname(String nickname);
}