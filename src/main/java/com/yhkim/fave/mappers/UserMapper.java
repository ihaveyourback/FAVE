package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    int insertUser(UserEntity user);

    UserEntity selectUserByEmail(@Param("email") String email);

    UserEntity selectUserByContact(@Param("contact") String contact);

    UserEntity selectUserByNickname(@Param("nickname") String nickname);

    int updateUser(UserEntity user);

    // 김범수

    UserEntity[] selectAllUser();

    UserEntity selectUserByEmailAdmin(@Param("userEmail") String userEmail);

    int updateWarning(UserEntity user);

    int selectUserCount();

    UserEntity[] selectUserPage(@Param(value = "limitCount") int limitCount,
                                @Param(value = "offsetCount") int offsetCount);

    int selectUserCountBySearch(@Param(value = "filter") String filter,
                                @Param(value = "keyword") String keyword);

    UserEntity[] selectUserBySearch(@Param(value = "filter") String filter,
                                    @Param(value = "keyword") String keyword,
                                    @Param(value = "limitCount") int limitCount,
                                    @Param(value = "offsetCount") int offsetCount);

}
