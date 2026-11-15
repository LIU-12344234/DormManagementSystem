package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.entity.Student;
import com.dorm.mapper.StudentMapper;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentMapper studentMapper;

    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    public Student requireByUserId(Long userId) {
        Student s = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
            .eq(Student::getUserId, userId)
            .last("limit 1"));
        if (s == null) {
            throw new IllegalArgumentException("当前账号未绑定学生信息");
        }
        return s;
    }
}
