package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.FaveInfoEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WriteMapper {

    int insertAdminWrite(FaveInfoEntity adminPage);
}
