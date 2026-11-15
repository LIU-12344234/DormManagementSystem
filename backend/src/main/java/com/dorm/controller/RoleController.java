package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.common.ApiResponse;
import com.dorm.entity.SysRole;
import com.dorm.mapper.SysRoleMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final SysRoleMapper sysRoleMapper;

    public RoleController(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SysRole>> list() {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getEnabled, 1)
            .orderByAsc(SysRole::getId);
        return ApiResponse.ok(sysRoleMapper.selectList(qw));
    }
}
