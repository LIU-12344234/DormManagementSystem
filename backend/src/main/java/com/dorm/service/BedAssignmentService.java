package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.entity.Bed;
import com.dorm.entity.BedAssignment;
import com.dorm.mapper.BedAssignmentMapper;
import com.dorm.mapper.BedMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BedAssignmentService {

    private final BedAssignmentMapper bedAssignmentMapper;
    private final BedMapper bedMapper;

    public BedAssignmentService(BedAssignmentMapper bedAssignmentMapper, BedMapper bedMapper) {
        this.bedAssignmentMapper = bedAssignmentMapper;
        this.bedMapper = bedMapper;
    }

    @Transactional
    public Long assign(Long studentId, Long bedId, String reason, Long operatorUserId) {
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            throw new IllegalArgumentException("床位不存在");
        }
        if (!"EMPTY".equalsIgnoreCase(bed.getStatus())) {
            throw new IllegalArgumentException("床位非空，无法分配");
        }

        BedAssignment active = bedAssignmentMapper.selectOne(new LambdaQueryWrapper<BedAssignment>()
            .eq(BedAssignment::getStudentId, studentId)
            .isNull(BedAssignment::getCheckOutAt)
            .last("limit 1"));
        if (active != null) {
            throw new IllegalArgumentException("该学生已有在住床位，请先退宿/调宿");
        }

        bed.setStatus("OCCUPIED");
        bedMapper.updateById(bed);

        BedAssignment a = new BedAssignment();
        a.setStudentId(studentId);
        a.setBedId(bedId);
        a.setReason(reason);
        a.setCreatedBy(operatorUserId);
        bedAssignmentMapper.insert(a);
        return a.getId();
    }

    @Transactional
    public void checkoutByStudent(Long studentId, String reason) {
        BedAssignment active = bedAssignmentMapper.selectOne(new LambdaQueryWrapper<BedAssignment>()
            .eq(BedAssignment::getStudentId, studentId)
            .isNull(BedAssignment::getCheckOutAt)
            .last("limit 1"));
        if (active == null) {
            throw new IllegalArgumentException("该学生无在住记录");
        }

        active.setCheckOutAt(java.time.LocalDateTime.now());
        if (reason != null && !reason.isBlank()) {
            active.setReason(reason);
        }
        bedAssignmentMapper.updateById(active);

        Bed bed = bedMapper.selectById(active.getBedId());
        if (bed != null && !"DISABLED".equalsIgnoreCase(bed.getStatus())) {
            bed.setStatus("EMPTY");
            bedMapper.updateById(bed);
        }
    }
}
