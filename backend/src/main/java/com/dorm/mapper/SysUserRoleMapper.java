package com.dorm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {

    @Insert("INSERT IGNORE INTO sys_user_role(user_id, role_id, created_at) VALUES(#{userId}, #{roleId}, NOW())")
    int insertIgnore(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("SELECT 1 FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId} LIMIT 1")
    Integer exists(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserAndRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
