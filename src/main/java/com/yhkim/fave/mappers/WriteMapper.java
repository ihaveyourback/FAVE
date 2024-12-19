package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.WriteEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WriteMapper {

    int insertAdminWrite(WriteEntity adminPage);
}
