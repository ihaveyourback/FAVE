package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insertUser(UserEntity user);
    UserEntity selectUserByEmail(@Param("email") String email); // 이메일로 사용자 조회
    UserEntity selectUserByContact(@Param("contact") String contact); // 연락처로 사용자 조회
    UserEntity selectUserByNickname(@Param("nickname") String nickname); // 닉네임으로 사용자 조회
    int updateUser(UserEntity user); // 사용자 정보 수정
}
