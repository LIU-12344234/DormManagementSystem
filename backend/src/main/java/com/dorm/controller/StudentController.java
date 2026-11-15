package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.ApiResponse;
import com.dorm.entity.Student;
import com.dorm.mapper.StudentMapper;
import com.dorm.security.CurrentUserService;
import com.dorm.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentMapper studentMapper;
    private final CurrentUserService currentUserService;
    private final StudentService studentService;

    public StudentController(StudentMapper studentMapper, CurrentUserService currentUserService, StudentService studentService) {
        this.studentMapper = studentMapper;
        this.currentUserService = currentUserService;
        this.studentService = studentService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Student> me(Authentication authentication) {
        Long userId = currentUserService.requireCurrentUser(authentication).getId();
        return ApiResponse.ok(studentService.requireByUserId(userId));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<List<Map<String, Object>>> options(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Student> qw = new LambdaQueryWrapper<Student>()
            .orderByAsc(Student::getStudentNo);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Student::getStudentNo, keyword)
                .or().like(Student::getName, keyword));
        }
        return ApiResponse.ok(studentMapper.selectList(qw).stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("studentNo", s.getStudentNo());
            m.put("name", s.getName());
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_MANAGER')")
    public ApiResponse<Page<Student>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Student> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Student::getStudentNo, keyword)
                .or().like(Student::getName, keyword)
                .or().like(Student::getClassName, keyword));
        }
        qw.orderByDesc(Student::getId);
        return ApiResponse.ok(studentMapper.selectPage(Page.of(page, size), qw));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> create(@RequestBody @Valid CreateReq req) {
        Student s = new Student();
        s.setUserId(req.getUserId());
        s.setStudentNo(req.getStudentNo());
        s.setName(req.getName());
        s.setGender(req.getGender());
        s.setCollege(req.getCollege());
        s.setMajor(req.getMajor());
        s.setClassName(req.getClassName());
        s.setPhone(req.getPhone());
        s.setIdCardNo(req.getIdCardNo());
        s.setStatus(req.getStatus() == null ? "ACTIVE" : req.getStatus());
        studentMapper.insert(s);
        return ApiResponse.ok(s.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateReq req) {
        Student s = studentMapper.selectById(id);
        if (s == null) {
            return ApiResponse.fail(404, "学生不存在");
        }
        s.setUserId(req.getUserId());
        s.setStudentNo(req.getStudentNo());
        s.setName(req.getName());
        s.setGender(req.getGender());
        s.setCollege(req.getCollege());
        s.setMajor(req.getMajor());
        s.setClassName(req.getClassName());
        s.setPhone(req.getPhone());
        s.setIdCardNo(req.getIdCardNo());
        s.setStatus(req.getStatus());
        studentMapper.updateById(s);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @Data
    public static class CreateReq {
        private Long userId;
        @NotBlank
        private String studentNo;
        @NotBlank
        private String name;
        @NotBlank
        private String gender;
        private String college;
        private String major;
        private String className;
        private String phone;
        private String idCardNo;
        private String status;
    }

    @Data
    public static class UpdateReq {
        private Long userId;
        @NotBlank
        private String studentNo;
        @NotBlank
        private String name;
        @NotBlank
        private String gender;
        private String college;
        private String major;
        private String className;
        private String phone;
        private String idCardNo;
        @NotBlank
        private String status;
    }
}
