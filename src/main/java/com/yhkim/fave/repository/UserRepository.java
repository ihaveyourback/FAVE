package com.yhkim.fave.repository;

import com.yhkim.fave.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    // ID 타입을 String으로 수정
    UserEntity findByNickname(String nickname);
    UserEntity findByContact(String contact);

    Optional<Object> findByEmail(String username);
}
