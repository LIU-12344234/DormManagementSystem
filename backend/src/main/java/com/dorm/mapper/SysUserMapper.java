package com.dorm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("\n" +
        "SELECT u.*\n" +
        "FROM sys_user u\n" +
        "JOIN sys_user_role ur ON ur.user_id = u.id\n" +
        "JOIN sys_role r ON r.id = ur.role_id\n" +
        "WHERE r.role_key = #{roleKey} AND u.enabled = 1")
    List<SysUser> selectUsersByRoleKey(@Param("roleKey") String roleKey);
}
