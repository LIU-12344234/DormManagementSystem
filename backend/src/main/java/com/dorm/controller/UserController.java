package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.SysRole;
import com.dorm.entity.SysUser;
import com.dorm.mapper.SysRoleMapper;
import com.dorm.mapper.SysUserMapper;
import com.dorm.mapper.SysUserRoleMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper, SysUserRoleMapper sysUserRoleMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "10") long size,
                                                       @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(SysUser::getUsername, keyword)
                .or().like(SysUser::getDisplayName, keyword)
                .or().like(SysUser::getPhone, keyword));
        }
        qw.orderByDesc(SysUser::getId);

        Page<SysUser> p = sysUserMapper.selectPage(Page.of(page, size), qw);
        Page<Map<String, Object>> out = Page.of(page, size);
        out.setTotal(p.getTotal());
        out.setPages(p.getPages());

        List<Map<String, Object>> records = new ArrayList<>();
        for (SysUser u : p.getRecords()) {
            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(u.getId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", u.getId());
            row.put("username", u.getUsername());
            row.put("displayName", u.getDisplayName());
            row.put("phone", u.getPhone());
            row.put("email", u.getEmail());
            row.put("enabled", u.getEnabled());
            row.put("roles", roles.stream().map(SysRole::getRoleKey).toList());
            records.add(row);
        }
        out.setRecords(records);
        return ApiResponse.ok(out);
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<List<Map<String, Object>>> options(@RequestParam String roleKey) {
        List<SysUser> users = sysUserMapper.selectUsersByRoleKey(roleKey);
        List<Map<String, Object>> out = new ArrayList<>();
        for (SysUser u : users) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", u.getId());
            row.put("username", u.getUsername());
            row.put("displayName", u.getDisplayName());
            out.add(row);
        }
        return ApiResponse.ok(out);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> create(@RequestBody @Valid CreateReq req) {
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setDisplayName(req.getDisplayName());
        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        u.setEnabled(req.getEnabled() == null ? 1 : req.getEnabled());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        sysUserMapper.insert(u);

        if (req.getRoleIds() != null) {
            for (Long rid : req.getRoleIds()) {
                sysUserRoleMapper.insertIgnore(u.getId(), rid);
            }
        }

        return ApiResponse.ok(u.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateReq req) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) return ApiResponse.fail(404, "用户不存在");
        u.setDisplayName(req.getDisplayName());
        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        u.setEnabled(req.getEnabled());
        sysUserMapper.updateById(u);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody @Valid ResetPwdReq req) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) return ApiResponse.fail(404, "用户不存在");
        u.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        sysUserMapper.updateById(u);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> setRoles(@PathVariable Long id, @RequestBody @Valid SetRolesReq req) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) return ApiResponse.fail(404, "用户不存在");
        sysUserRoleMapper.deleteByUserId(id);
        for (Long rid : req.getRoleIds()) {
            sysUserRoleMapper.insertIgnore(id, rid);
        }
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateReq {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String displayName;
        private String phone;
        private String email;
        private Integer enabled;
        private List<Long> roleIds;
    }

    @Data
    public static class UpdateReq {
        @NotBlank
        private String displayName;
        private String phone;
        private String email;
        @NotNull
        private Integer enabled;
    }

    @Data
    public static class ResetPwdReq {
        @NotBlank
        private String newPassword;
    }

    @Data
    public static class SetRolesReq {
        @NotNull
        private List<Long> roleIds;
    }
}
