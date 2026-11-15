package com.dorm.controller;

import com.dorm.common.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public ApiResponse<Map<String, Object>> me(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", authentication.getName());
        data.put("authorities", authentication.getAuthorities());
        return ApiResponse.ok(data);
    }
}
