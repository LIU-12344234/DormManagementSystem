package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.DormRoom;
import com.dorm.mapper.DormRoomMapper;
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
@RequestMapping("/api/rooms")
public class DormRoomController {

    private final DormRoomMapper dormRoomMapper;

    public DormRoomController(DormRoomMapper dormRoomMapper) {
        this.dormRoomMapper = dormRoomMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Page<DormRoom>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) Long buildingId,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DormRoom> qw = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            qw.eq(DormRoom::getBuildingId, buildingId);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(DormRoom::getRoomNo, keyword);
        }
        qw.orderByDesc(DormRoom::getId);
        return ApiResponse.ok(dormRoomMapper.selectPage(Page.of(page, size), qw));
    }

    @GetMapping("/options")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> options(@RequestParam(required = false) Long buildingId) {
        LambdaQueryWrapper<DormRoom> qw = new LambdaQueryWrapper<DormRoom>()
            .eq(buildingId != null, DormRoom::getBuildingId, buildingId)
            .eq(DormRoom::getEnabled, 1)
            .orderByAsc(DormRoom::getBuildingId)
            .orderByAsc(DormRoom::getFloorNo)
            .orderByAsc(DormRoom::getRoomNo);

        return ApiResponse.ok(dormRoomMapper.selectList(qw).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("buildingId", r.getBuildingId());
            m.put("floorNo", r.getFloorNo());
            m.put("roomNo", r.getRoomNo());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Long> create(@RequestBody @Valid CreateReq req) {
        DormRoom r = new DormRoom();
        r.setBuildingId(req.getBuildingId());
        r.setFloorNo(req.getFloorNo());
        r.setRoomNo(req.getRoomNo());
        r.setRoomType(req.getRoomType() == null ? "NORMAL" : req.getRoomType());
        r.setCapacity(req.getCapacity() == null ? 4 : req.getCapacity());
        r.setGenderLimit(req.getGenderLimit() == null ? "MIX" : req.getGenderLimit());
        r.setEnabled(req.getEnabled() == null ? 1 : req.getEnabled());
        dormRoomMapper.insert(r);
        return ApiResponse.ok(r.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateReq req) {
        DormRoom r = dormRoomMapper.selectById(id);
        if (r == null) {
            return ApiResponse.fail(404, "房间不存在");
        }
        r.setBuildingId(req.getBuildingId());
        r.setFloorNo(req.getFloorNo());
        r.setRoomNo(req.getRoomNo());
        r.setRoomType(req.getRoomType());
        r.setCapacity(req.getCapacity());
        r.setGenderLimit(req.getGenderLimit());
        r.setEnabled(req.getEnabled());
        dormRoomMapper.updateById(r);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dormRoomMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateReq {
        @NotNull
        private Long buildingId;
        @NotNull
        private Integer floorNo;
        @NotBlank
        private String roomNo;
        private String roomType;
        private Integer capacity;
        private String genderLimit;
        private Integer enabled;
    }

    @Data
    public static class UpdateReq {
        @NotNull
        private Long buildingId;
        @NotNull
        private Integer floorNo;
        @NotBlank
        private String roomNo;
        @NotBlank
        private String roomType;
        @NotNull
        private Integer capacity;
        @NotBlank
        private String genderLimit;
        @NotNull
        private Integer enabled;
    }
}
