package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.FaveInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FaveInfoMapper {

    FaveInfoEntity selectFaveInfoById(@Param("index") int index);

    int selectFaveInfoCount();

    FaveInfoEntity[] selectFaveInfo(@Param(value = "limitCount") int limitCount,
                                    @Param(value = "offsetCount") int offsetCount);

    int updateFaveInfoView(FaveInfoEntity faveInfo);

    int updateFaveInfo(FaveInfoEntity faveInfo);
}
