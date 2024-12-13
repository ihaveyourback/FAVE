package com.yhkim.fave.repository;

import com.yhkim.fave.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    // 올바른 메서드 정의
    UserEntity findByNickname(String nickname);
    UserEntity findByContact(String contact);

    // 반환 타입 수정: Optional<UserEntity>
    Optional<UserEntity> findByEmail(String username);
}
