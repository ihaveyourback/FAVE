package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportsMapper {

    int selectReportsCount();

    Report[] selectReports(@Param(value = "limitCount") int limitCount,
                           @Param(value = "offsetCount") int offsetCount);

    Report selectReportByIndex(@Param("index") int index);

    int updateReport(Report report);
}
