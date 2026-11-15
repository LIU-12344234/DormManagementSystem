package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.VisitorRecord;
import com.dorm.mapper.VisitorRecordMapper;
import com.dorm.security.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private final VisitorRecordMapper visitorRecordMapper;
    private final CurrentUserService currentUserService;

    public VisitorController(VisitorRecordMapper visitorRecordMapper, CurrentUserService currentUserService) {
        this.visitorRecordMapper = visitorRecordMapper;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Page<VisitorRecord>> page(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) Long buildingId,
                                                 @RequestParam(required = false) Boolean activeOnly) {
        LambdaQueryWrapper<VisitorRecord> qw = new LambdaQueryWrapper<>();
        if (buildingId != null) qw.eq(VisitorRecord::getBuildingId, buildingId);
        if (activeOnly != null && activeOnly) qw.isNull(VisitorRecord::getOutTime);
        qw.orderByDesc(VisitorRecord::getId);
        return ApiResponse.ok(visitorRecordMapper.selectPage(Page.of(page, size), qw));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Long> create(@RequestBody @Valid CreateReq req, Authentication authentication) {
        Long registrar = currentUserService.requireCurrentUser(authentication).getId();
        VisitorRecord r = new VisitorRecord();
        r.setBuildingId(req.getBuildingId());
        r.setRoomId(req.getRoomId());
        r.setVisitedStudentId(req.getVisitedStudentId());
        r.setVisitorName(req.getVisitorName());
        r.setVisitorPhone(req.getVisitorPhone());
        r.setVisitorIdCard(req.getVisitorIdCard());
        r.setRelationDesc(req.getRelationDesc());
        r.setVisitReason(req.getVisitReason());
        r.setRegistrarUserId(registrar);
        visitorRecordMapper.insert(r);
        return ApiResponse.ok(r.getId());
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Void> checkout(@PathVariable Long id) {
        VisitorRecord r = visitorRecordMapper.selectById(id);
        if (r == null) return ApiResponse.fail(404, "记录不存在");
        r.setOutTime(LocalDateTime.now());
        visitorRecordMapper.updateById(r);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        visitorRecordMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateReq {
        @NotNull
        private Long buildingId;
        private Long roomId;
        private Long visitedStudentId;
        @NotBlank
        private String visitorName;
        private String visitorPhone;
        private String visitorIdCard;
        private String relationDesc;
        private String visitReason;
    }
}
