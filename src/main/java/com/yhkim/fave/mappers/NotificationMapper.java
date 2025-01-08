package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.NotificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper {
    int insert(NotificationEntity entity);

    NotificationEntity select(@Param("index") int index);

    NotificationEntity[] selectAll(@Param("userEmail") String userEmail);

    int update(NotificationEntity entity);
}
