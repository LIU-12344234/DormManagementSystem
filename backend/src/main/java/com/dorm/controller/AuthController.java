package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.common.ApiResponse;
import com.dorm.entity.SysRole;
import com.dorm.entity.SysUser;
import com.dorm.mapper.SysRoleMapper;
import com.dorm.mapper.SysUserMapper;
import com.dorm.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResp> login(@RequestBody @Valid LoginReq req) {
        SysUser u = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, req.getUsername())
            .eq(SysUser::getEnabled, 1)
            .last("limit 1"));

        if (u == null) {
            return ApiResponse.fail(401, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            return ApiResponse.fail(401, "用户名或密码错误");
        }

        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(u.getId());
        List<String> roleKeys = roles.stream().map(SysRole::getRoleKey).toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", u.getId());
        claims.put("roles", roleKeys);

        String token = jwtService.generateToken(u.getUsername(), claims);
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUsername(u.getUsername());
        resp.setDisplayName(u.getDisplayName());
        resp.setRoles(roleKeys);
        return ApiResponse.ok(resp);
    }

    @Data
    public static class LoginReq {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @Data
    public static class LoginResp {
        private String token;
        private String username;
        private String displayName;
        private List<String> roles;
    }
}
