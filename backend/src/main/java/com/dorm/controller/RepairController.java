package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.RepairLog;
import com.dorm.entity.RepairOrder;
import com.dorm.entity.Student;
import com.dorm.mapper.RepairLogMapper;
import com.dorm.mapper.RepairOrderMapper;
import com.dorm.security.CurrentUserService;
import com.dorm.service.RepairService;
import com.dorm.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repairs")
public class RepairController {

    private final RepairOrderMapper repairOrderMapper;
    private final RepairLogMapper repairLogMapper;
    private final RepairService repairService;
    private final CurrentUserService currentUserService;
    private final StudentService studentService;

    public RepairController(RepairOrderMapper repairOrderMapper, RepairLogMapper repairLogMapper, RepairService repairService, CurrentUserService currentUserService, StudentService studentService) {
        this.repairOrderMapper = repairOrderMapper;
        this.repairLogMapper = repairLogMapper;
        this.repairService = repairService;
        this.currentUserService = currentUserService;
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER','WORKER')")
    public ApiResponse<Page<RepairOrder>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(required = false) Long roomId,
                                               @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<RepairOrder> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) qw.eq(RepairOrder::getStatus, status);
        if (roomId != null) qw.eq(RepairOrder::getRoomId, roomId);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(RepairOrder::getOrderNo, keyword)
                .or().like(RepairOrder::getTitle, keyword));
        }
        qw.orderByDesc(RepairOrder::getId);
        return ApiResponse.ok(repairOrderMapper.selectPage(Page.of(page, size), qw));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Page<RepairOrder>> my(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             Authentication authentication) {
        Long userId = currentUserService.requireCurrentUser(authentication).getId();
        Student s = studentService.requireByUserId(userId);
        LambdaQueryWrapper<RepairOrder> qw = new LambdaQueryWrapper<RepairOrder>()
            .eq(RepairOrder::getStudentId, s.getId())
            .orderByDesc(RepairOrder::getId);
        return ApiResponse.ok(repairOrderMapper.selectPage(Page.of(page, size), qw));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Long> submit(@RequestBody @Valid SubmitReq req, Authentication authentication) {
        Long userId = currentUserService.requireCurrentUser(authentication).getId();
        Student s = studentService.requireByUserId(userId);
        return ApiResponse.ok(repairService.submit(s.getId(), req.getRoomId(), req.getTitle(), req.getDescription(), req.getPhotosJson(), req.getPriority()));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Void> assign(@PathVariable Long id, @RequestBody @Valid AssignReq req, Authentication authentication) {
        Long operatorUserId = currentUserService.requireCurrentUser(authentication).getId();
        repairService.assign(id, req.getWorkerUserId(), operatorUserId, req.getNote());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('WORKER')")
    public ApiResponse<Void> start(@PathVariable Long id, @RequestBody(required = false) NoteReq req, Authentication authentication) {
        Long workerUserId = currentUserService.requireCurrentUser(authentication).getId();
        repairService.start(id, workerUserId, req == null ? null : req.getNote());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('WORKER')")
    public ApiResponse<Void> finish(@PathVariable Long id, @RequestBody(required = false) NoteReq req, Authentication authentication) {
        Long workerUserId = currentUserService.requireCurrentUser(authentication).getId();
        repairService.finish(id, workerUserId, req == null ? null : req.getNote());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Void> close(@PathVariable Long id, @RequestBody(required = false) NoteReq req, Authentication authentication) {
        Long operatorUserId = currentUserService.requireCurrentUser(authentication).getId();
        repairService.close(id, operatorUserId, req == null ? null : req.getNote());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> rate(@PathVariable Long id, @RequestBody @Valid RateReq req, Authentication authentication) {
        Long userId = currentUserService.requireCurrentUser(authentication).getId();
        Student s = studentService.requireByUserId(userId);
        repairService.rate(id, s.getId(), userId, req.getRating());
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER','WORKER','STUDENT')")
    public ApiResponse<List<RepairLog>> logs(@PathVariable Long id) {
        LambdaQueryWrapper<RepairLog> qw = new LambdaQueryWrapper<RepairLog>()
            .eq(RepairLog::getRepairOrderId, id)
            .orderByAsc(RepairLog::getId);
        return ApiResponse.ok(repairLogMapper.selectList(qw));
    }

    @Data
    public static class SubmitReq {
        @NotNull
        private Long roomId;
        @NotBlank
        private String title;
        private String description;
        private String photosJson;
        private String priority;
    }

    @Data
    public static class AssignReq {
        @NotNull
        private Long workerUserId;
        private String note;
    }

    @Data
    public static class NoteReq {
        private String note;
    }

    @Data
    public static class RateReq {
        @NotNull
        private Integer rating;
    }
}
