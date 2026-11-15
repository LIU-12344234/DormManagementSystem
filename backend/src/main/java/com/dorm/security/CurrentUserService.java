package com.dorm.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.entity.SysUser;
import com.dorm.mapper.SysUserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final SysUserMapper sysUserMapper;

    public CurrentUserService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    public SysUser requireCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        SysUser u = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, username)
            .last("limit 1"));
        if (u == null) {
            throw new IllegalStateException("Current user not found");
        }
        return u;
    }
}
