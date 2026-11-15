package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.BedAssignment;
import com.dorm.entity.Student;
import com.dorm.mapper.BedAssignmentMapper;
import com.dorm.security.CurrentUserService;
import com.dorm.service.BedAssignmentService;
import com.dorm.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
public class BedAssignmentController {

    private final BedAssignmentMapper bedAssignmentMapper;
    private final BedAssignmentService bedAssignmentService;
    private final CurrentUserService currentUserService;
    private final StudentService studentService;

    public BedAssignmentController(BedAssignmentMapper bedAssignmentMapper, BedAssignmentService bedAssignmentService, CurrentUserService currentUserService, StudentService studentService) {
        this.bedAssignmentMapper = bedAssignmentMapper;
        this.bedAssignmentService = bedAssignmentService;
        this.currentUserService = currentUserService;
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Page<BedAssignment>> page(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) Long studentId,
                                                 @RequestParam(required = false) Long bedId,
                                                 @RequestParam(required = false) Boolean activeOnly) {
        LambdaQueryWrapper<BedAssignment> qw = new LambdaQueryWrapper<>();
        if (studentId != null) qw.eq(BedAssignment::getStudentId, studentId);
        if (bedId != null) qw.eq(BedAssignment::getBedId, bedId);
        if (activeOnly != null && activeOnly) qw.isNull(BedAssignment::getCheckOutAt);
        qw.orderByDesc(BedAssignment::getId);
        return ApiResponse.ok(bedAssignmentMapper.selectPage(Page.of(page, size), qw));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Long> assign(@RequestBody @Valid AssignReq req, Authentication authentication) {
        Long operatorUserId = currentUserService.requireCurrentUser(authentication).getId();
        return ApiResponse.ok(bedAssignmentService.assign(req.getStudentId(), req.getBedId(), req.getReason(), operatorUserId));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Void> checkout(@RequestBody @Valid CheckoutReq req) {
        bedAssignmentService.checkoutByStudent(req.getStudentId(), req.getReason());
        return ApiResponse.ok(null);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Page<BedAssignment>> my(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               Authentication authentication) {
        Long userId = currentUserService.requireCurrentUser(authentication).getId();
        Student s = studentService.requireByUserId(userId);
        LambdaQueryWrapper<BedAssignment> qw = new LambdaQueryWrapper<BedAssignment>()
            .eq(BedAssignment::getStudentId, s.getId())
            .orderByDesc(BedAssignment::getId);
        return ApiResponse.ok(bedAssignmentMapper.selectPage(Page.of(page, size), qw));
    }

    @Data
    public static class AssignReq {
        @NotNull
        private Long studentId;
        @NotNull
        private Long bedId;
        private String reason;
    }

    @Data
    public static class CheckoutReq {
        @NotNull
        private Long studentId;
        private String reason;
    }
}
