package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.LateReturnRecord;
import com.dorm.mapper.LateReturnRecordMapper;
import com.dorm.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/late-returns")
public class LateReturnController {

    private final LateReturnRecordMapper lateReturnRecordMapper;
    private final CurrentUserService currentUserService;

    public LateReturnController(LateReturnRecordMapper lateReturnRecordMapper, CurrentUserService currentUserService) {
        this.lateReturnRecordMapper = lateReturnRecordMapper;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Page<LateReturnRecord>> page(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size,
                                                    @RequestParam(required = false) Long studentId) {
        LambdaQueryWrapper<LateReturnRecord> qw = new LambdaQueryWrapper<>();
        if (studentId != null) qw.eq(LateReturnRecord::getStudentId, studentId);
        qw.orderByDesc(LateReturnRecord::getId);
        return ApiResponse.ok(lateReturnRecordMapper.selectPage(Page.of(page, size), qw));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Long> create(@RequestBody @Valid CreateReq req, Authentication authentication) {
        Long handler = currentUserService.requireCurrentUser(authentication).getId();
        LateReturnRecord r = new LateReturnRecord();
        r.setStudentId(req.getStudentId());
        r.setReason(req.getReason());
        r.setHandledBy(handler);
        lateReturnRecordMapper.insert(r);
        return ApiResponse.ok(r.getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        lateReturnRecordMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateReq {
        @NotNull
        private Long studentId;
        private String reason;
    }
}
