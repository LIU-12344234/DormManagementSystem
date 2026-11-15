package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.Notification;
import com.dorm.mapper.NotificationMapper;
import com.dorm.security.CurrentUserService;
import com.dorm.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
public class NotificationController {

    private final NotificationMapper notificationMapper;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    public NotificationController(NotificationMapper notificationMapper, NotificationService notificationService, CurrentUserService currentUserService) {
        this.notificationMapper = notificationMapper;
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<Notification>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Notification::getTitle, keyword));
        }
        qw.orderByDesc(Notification::getId);
        return ApiResponse.ok(notificationMapper.selectPage(Page.of(page, size), qw));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Long> publish(@RequestBody @Valid PublishReq req, Authentication authentication) {
        Long publisher = currentUserService.requireCurrentUser(authentication).getId();
        Notification n = new Notification();
        n.setTitle(req.getTitle());
        n.setContent(req.getContent());
        n.setTargetType(req.getTargetType() == null ? "ALL" : req.getTargetType());
        n.setTargetValue(req.getTargetValue());
        n.setPublisherUserId(publisher);
        notificationMapper.insert(n);
        return ApiResponse.ok(n.getId());
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> read(@PathVariable Long id, Authentication authentication) {
        Long userId = currentUserService.requireCurrentUser(authentication).getId();
        notificationService.markRead(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", id);
        data.put("read", true);
        return ApiResponse.ok(data);
    }

    @GetMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> readState(@PathVariable Long id, Authentication authentication) {
        Long userId = currentUserService.requireCurrentUser(authentication).getId();
        boolean read = notificationService.isRead(id, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", id);
        data.put("read", read);
        return ApiResponse.ok(data);
    }

    @Data
    public static class PublishReq {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        private String targetType;
        private String targetValue;
    }
}
