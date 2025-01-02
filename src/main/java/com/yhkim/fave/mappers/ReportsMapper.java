package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.ReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportsMapper {

    int selectReportsCount();

    ReportEntity[] selectReports(@Param(value = "limitCount") int limitCount,
                                 @Param(value = "offsetCount") int offsetCount);

    ReportEntity selectReportByIndex(@Param("index") int index);

    int updateReport(ReportEntity reportEntity);
}
