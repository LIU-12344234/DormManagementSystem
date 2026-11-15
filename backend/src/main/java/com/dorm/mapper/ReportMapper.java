package com.dorm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReportMapper {

    @Select("SELECT COUNT(1) FROM bed")
    long totalBeds();

    @Select("SELECT COUNT(1) FROM bed WHERE status = 'OCCUPIED'")
    long occupiedBeds();

    @Select("SELECT COUNT(1) FROM repair_order")
    long totalRepairs();

    @Select("SELECT COUNT(1) FROM repair_order WHERE status = 'SUBMITTED'")
    long repairsSubmitted();

    @Select("SELECT COUNT(1) FROM repair_order WHERE status = 'ASSIGNED'")
    long repairsAssigned();

    @Select("SELECT COUNT(1) FROM repair_order WHERE status = 'IN_PROGRESS'")
    long repairsInProgress();

    @Select("SELECT COUNT(1) FROM repair_order WHERE status = 'FINISHED'")
    long repairsFinished();

    @Select("SELECT COUNT(1) FROM repair_order WHERE status = 'CLOSED'")
    long repairsClosed();

    @Select("SELECT COUNT(1) FROM late_return_record WHERE DATE(late_time) = CURDATE()")
    long lateReturnsToday();
}
