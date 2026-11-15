package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.SystemSetting;
import com.dorm.mapper.SystemSettingMapper;
import com.dorm.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SystemSettingController {

    private final SystemSettingMapper systemSettingMapper;
    private final CurrentUserService currentUserService;

    public SystemSettingController(SystemSettingMapper systemSettingMapper, CurrentUserService currentUserService) {
        this.systemSettingMapper = systemSettingMapper;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<SystemSetting>> page(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SystemSetting> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(SystemSetting::getSettingKey, keyword)
                .or().like(SystemSetting::getDescription, keyword));
        }
        qw.orderByDesc(SystemSetting::getId);
        return ApiResponse.ok(systemSettingMapper.selectPage(Page.of(page, size), qw));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> create(@RequestBody @Valid CreateReq req, Authentication authentication) {
        Long uid = currentUserService.requireCurrentUser(authentication).getId();
        SystemSetting s = new SystemSetting();
        s.setSettingKey(req.getSettingKey());
        s.setSettingValue(req.getSettingValue());
        s.setDescription(req.getDescription());
        s.setUpdatedBy(uid);
        systemSettingMapper.insert(s);
        return ApiResponse.ok(s.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateReq req, Authentication authentication) {
        Long uid = currentUserService.requireCurrentUser(authentication).getId();
        SystemSetting s = systemSettingMapper.selectById(id);
        if (s == null) return ApiResponse.fail(404, "配置不存在");
        s.setSettingKey(req.getSettingKey());
        s.setSettingValue(req.getSettingValue());
        s.setDescription(req.getDescription());
        s.setUpdatedBy(uid);
        systemSettingMapper.updateById(s);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        systemSettingMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateReq {
        @NotBlank
        private String settingKey;
        private String settingValue;
        private String description;
    }

    @Data
    public static class UpdateReq {
        @NotBlank
        private String settingKey;
        private String settingValue;
        private String description;
    }
}
