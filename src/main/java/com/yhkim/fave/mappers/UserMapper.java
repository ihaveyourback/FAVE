package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.FaveInfoEntity;
import com.yhkim.fave.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    int insertUser(UserEntity user);
    UserEntity selectUserByEmail(@Param("email") String email); // 이메일로 사용자 조회
    UserEntity selectUserByContact(@Param("contact") String contact); // 연락처로 사용자 조회
    UserEntity selectUserByNickname(@Param("nickname") String nickname); // 닉네임으로 사용자 조회
    int updateUser(UserEntity user); // 사용자 정보 수정



    UserEntity[] selectAllUser();

    UserEntity selectUserByEmailAdmin(@Param("userEmail") String userEmail);

    int updateWarning(UserEntity user);

    int selectUserCount();

    UserEntity[] selectUserPage(@Param(value = "limitCount") int limitCount,
                                @Param(value = "offsetCount") int offsetCount);

    int selectUserCountBySearch(@Param(value = "filter") String filter,
                                @Param(value = "keyword") String keyword);

    // 사용자 검색 (페이징 처리) - filter: 검색 조건, keyword: 검색어
    UserEntity[] selectUserBySearch(@Param(value = "filter") String filter,
                                    @Param(value = "keyword") String keyword,
                                    @Param(value = "limitCount") int limitCount,
                                    @Param(value = "offsetCount") int offsetCount);


    List<FaveInfoEntity> selectFavoritePostsByUserEmail(String email); // 사용자의 찜 목록 조회

    List<FaveInfoEntity> selectFavoritePostsByUserEmailWithPagination(@Param("email") String email,
                                                                      @Param("limitCount") int limitCount,
                                                                      @Param("offsetCount") int offsetCount); // 사용자의 찜 목록 조회 (페이징 처리)
}
