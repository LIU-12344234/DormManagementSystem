package com.dorm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("\n" +
        "SELECT r.*\n" +
        "FROM sys_role r\n" +
        "JOIN sys_user_role ur ON ur.role_id = r.id\n" +
        "WHERE ur.user_id = #{userId} AND r.enabled = 1")
    List<SysRole> selectRolesByUserId(Long userId);
}
