package com.dorm.controller;

import com.dorm.common.ApiResponse;
import com.dorm.mapper.ReportMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportMapper reportMapper;

    public ReportController(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> overview() {
        long totalBeds = reportMapper.totalBeds();
        long occupiedBeds = reportMapper.occupiedBeds();

        Map<String, Object> data = new HashMap<>();
        data.put("totalBeds", totalBeds);
        data.put("occupiedBeds", occupiedBeds);
        data.put("emptyBeds", Math.max(0, totalBeds - occupiedBeds));
        data.put("occupancyRate", totalBeds == 0 ? 0.0 : (occupiedBeds * 1.0 / totalBeds));

        data.put("totalRepairs", reportMapper.totalRepairs());
        data.put("repairsSubmitted", reportMapper.repairsSubmitted());
        data.put("repairsAssigned", reportMapper.repairsAssigned());
        data.put("repairsInProgress", reportMapper.repairsInProgress());
        data.put("repairsFinished", reportMapper.repairsFinished());
        data.put("repairsClosed", reportMapper.repairsClosed());

        data.put("lateReturnsToday", reportMapper.lateReturnsToday());
        return ApiResponse.ok(data);
    }
}
