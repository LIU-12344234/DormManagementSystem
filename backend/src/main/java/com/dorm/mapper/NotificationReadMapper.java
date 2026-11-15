package com.dorm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface NotificationReadMapper {

    @Insert("INSERT IGNORE INTO notification_read(notification_id, user_id, read_at) VALUES(#{notificationId}, #{userId}, NOW())")
    int insertIgnore(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    @Select("SELECT 1 FROM notification_read WHERE notification_id = #{notificationId} AND user_id = #{userId} LIMIT 1")
    Integer exists(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
}
