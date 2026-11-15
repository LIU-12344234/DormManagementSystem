package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.Bed;
import com.dorm.mapper.BedMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/beds")
public class BedController {

    private final BedMapper bedMapper;

    public BedController(BedMapper bedMapper) {
        this.bedMapper = bedMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Page<Bed>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Long roomId,
                                       @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Bed> qw = new LambdaQueryWrapper<>();
        if (roomId != null) {
            qw.eq(Bed::getRoomId, roomId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(Bed::getStatus, status);
        }
        qw.orderByDesc(Bed::getId);
        return ApiResponse.ok(bedMapper.selectPage(Page.of(page, size), qw));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<List<Map<String, Object>>> options(@RequestParam(required = false) Long roomId,
                                                          @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Bed> qw = new LambdaQueryWrapper<Bed>()
            .eq(roomId != null, Bed::getRoomId, roomId)
            .eq(status != null && !status.isBlank(), Bed::getStatus, status)
            .orderByAsc(Bed::getRoomId)
            .orderByAsc(Bed::getBedNo);

        return ApiResponse.ok(bedMapper.selectList(qw).stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("roomId", b.getRoomId());
            m.put("bedNo", b.getBedNo());
            m.put("status", b.getStatus());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Long> create(@RequestBody @Valid CreateReq req) {
        Bed b = new Bed();
        b.setRoomId(req.getRoomId());
        b.setBedNo(req.getBedNo());
        b.setStatus(req.getStatus() == null ? "EMPTY" : req.getStatus());
        bedMapper.insert(b);
        return ApiResponse.ok(b.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateReq req) {
        Bed b = bedMapper.selectById(id);
        if (b == null) {
            return ApiResponse.fail(404, "床位不存在");
        }
        b.setRoomId(req.getRoomId());
        b.setBedNo(req.getBedNo());
        b.setStatus(req.getStatus());
        bedMapper.updateById(b);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bedMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateReq {
        @NotNull
        private Long roomId;
        @NotBlank
        private String bedNo;
        private String status;
    }

    @Data
    public static class UpdateReq {
        @NotNull
        private Long roomId;
        @NotBlank
        private String bedNo;
        @NotBlank
        private String status;
    }
}
