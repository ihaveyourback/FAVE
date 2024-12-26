package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.EmailTokenEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailTokenMapper {
    int insertEmailToken(EmailTokenEntity emailToken); // 이메일 토큰 추가

    EmailTokenEntity selectEmailTokenByUserEmailAndKey(@Param("userEmail") String userEmail,
                                                       @Param("key") String key); // 사용자 이메일과 키로 이메일 토큰 조회
    int updateEmailToken(EmailTokenEntity emailToken); // 이메일 토큰 업데이트
}
