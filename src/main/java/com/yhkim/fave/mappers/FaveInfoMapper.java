package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.FaveInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FaveInfoMapper {

    FaveInfoEntity selectFaveInfoById(@Param("index") int index);

    int selectFaveInfoCount();

    FaveInfoEntity[] selectFaveInfo(@Param(value = "limitCount") int limitCount,
                                    @Param(value = "offsetCount") int offsetCount);

    int updateFaveInfoView(FaveInfoEntity faveInfo);

    int updateFaveInfo(FaveInfoEntity faveInfo);

    List<FaveInfoEntity> searchFaveInfo(@Param("filter") String filter, @Param("keyword") String keyword, @Param("limitCount") int limitCount, @Param("offsetCount") int offsetCount);

    int selectFaveInfoCountBySearch(String filter, String keyword);
}
