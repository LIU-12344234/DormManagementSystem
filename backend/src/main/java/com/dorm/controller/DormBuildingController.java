package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.DormBuilding;
import com.dorm.mapper.DormBuildingMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buildings")
public class DormBuildingController {

    private final DormBuildingMapper dormBuildingMapper;

    public DormBuildingController(DormBuildingMapper dormBuildingMapper) {
        this.dormBuildingMapper = dormBuildingMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Page<DormBuilding>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DormBuilding> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(DormBuilding::getBuildingCode, keyword)
                .or().like(DormBuilding::getBuildingName, keyword));
        }
        qw.orderByDesc(DormBuilding::getId);
        return ApiResponse.ok(dormBuildingMapper.selectPage(Page.of(page, size), qw));
    }

    @GetMapping("/options")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> options() {
        LambdaQueryWrapper<DormBuilding> qw = new LambdaQueryWrapper<DormBuilding>()
            .eq(DormBuilding::getEnabled, 1)
            .orderByAsc(DormBuilding::getBuildingCode);

        return ApiResponse.ok(dormBuildingMapper.selectList(qw).stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("buildingCode", b.getBuildingCode());
            m.put("buildingName", b.getBuildingName());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> create(@RequestBody @Valid CreateReq req) {
        DormBuilding b = new DormBuilding();
        b.setBuildingCode(req.getBuildingCode());
        b.setBuildingName(req.getBuildingName());
        b.setAddress(req.getAddress());
        b.setManagerUserId(req.getManagerUserId());
        b.setEnabled(req.getEnabled() == null ? 1 : req.getEnabled());
        dormBuildingMapper.insert(b);
        return ApiResponse.ok(b.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateReq req) {
        DormBuilding b = dormBuildingMapper.selectById(id);
        if (b == null) {
            return ApiResponse.fail(404, "楼栋不存在");
        }
        b.setBuildingCode(req.getBuildingCode());
        b.setBuildingName(req.getBuildingName());
        b.setAddress(req.getAddress());
        b.setManagerUserId(req.getManagerUserId());
        b.setEnabled(req.getEnabled() == null ? b.getEnabled() : req.getEnabled());
        dormBuildingMapper.updateById(b);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dormBuildingMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateReq {
        @NotBlank
        private String buildingCode;
        @NotBlank
        private String buildingName;
        private String address;
        private Long managerUserId;
        private Integer enabled;
    }

    @Data
    public static class UpdateReq {
        @NotBlank
        private String buildingCode;
        @NotBlank
        private String buildingName;
        private String address;
        private Long managerUserId;
        private Integer enabled;
    }
}
