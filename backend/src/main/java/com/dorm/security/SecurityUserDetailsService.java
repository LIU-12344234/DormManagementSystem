package com.dorm.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.entity.SysRole;
import com.dorm.entity.SysUser;
import com.dorm.mapper.SysRoleMapper;
import com.dorm.mapper.SysUserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    public SecurityUserDetailsService(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser u = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, username)
            .eq(SysUser::getEnabled, 1)
            .last("limit 1"));

        if (u == null) {
            throw new UsernameNotFoundException("User not found");
        }

        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(u.getId());
        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleKey()))
            .toList();

        return User.builder()
            .username(u.getUsername())
            .password(u.getPasswordHash())
            .disabled(u.getEnabled() == null || u.getEnabled() == 0)
            .authorities(authorities)
            .build();
    }
}
