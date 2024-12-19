package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.ReportsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportsMapper {

    int selectReportsCount();

    ReportsEntity[] selectReports(@Param(value = "limitCount") int limitCount,
                                  @Param(value = "offsetCount") int offsetCount);

    ReportsEntity selectReportByIndex(@Param("index") int index);

    int updateReport(ReportsEntity report);
}
