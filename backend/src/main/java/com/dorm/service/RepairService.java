package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.entity.RepairLog;
import com.dorm.entity.RepairOrder;
import com.dorm.mapper.RepairLogMapper;
import com.dorm.mapper.RepairOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RepairService {

    private final RepairOrderMapper repairOrderMapper;
    private final RepairLogMapper repairLogMapper;

    public RepairService(RepairOrderMapper repairOrderMapper, RepairLogMapper repairLogMapper) {
        this.repairOrderMapper = repairOrderMapper;
        this.repairLogMapper = repairLogMapper;
    }

    public String newOrderNo() {
        return "R" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Transactional
    public Long submit(Long studentId, Long roomId, String title, String description, String photosJson, String priority) {
        RepairOrder o = new RepairOrder();
        o.setOrderNo(newOrderNo());
        o.setStudentId(studentId);
        o.setRoomId(roomId);
        o.setTitle(title);
        o.setDescription(description);
        o.setPhotosJson(photosJson);
        o.setPriority(priority == null || priority.isBlank() ? "NORMAL" : priority);
        o.setStatus("SUBMITTED");
        repairOrderMapper.insert(o);

        appendLog(o.getId(), "SUBMIT", null, "SUBMITTED", "提交报修", null);
        return o.getId();
    }

    @Transactional
    public void assign(Long orderId, Long workerUserId, Long operatorUserId, String note) {
        RepairOrder o = requireOrder(orderId);
        if (!"SUBMITTED".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalArgumentException("仅已提交工单可派单");
        }
        String from = o.getStatus();
        o.setAssignedWorkerUserId(workerUserId);
        o.setAssignedAt(LocalDateTime.now());
        o.setStatus("ASSIGNED");
        repairOrderMapper.updateById(o);
        appendLog(orderId, "ASSIGN", from, "ASSIGNED", note, operatorUserId);
    }

    @Transactional
    public void start(Long orderId, Long workerUserId, String note) {
        RepairOrder o = requireOrder(orderId);
        if (o.getAssignedWorkerUserId() == null || !o.getAssignedWorkerUserId().equals(workerUserId)) {
            throw new IllegalArgumentException("非当前工单维修工");
        }
        if (!"ASSIGNED".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalArgumentException("仅已派单工单可开始处理");
        }
        String from = o.getStatus();
        o.setStatus("IN_PROGRESS");
        repairOrderMapper.updateById(o);
        appendLog(orderId, "START", from, "IN_PROGRESS", note, workerUserId);
    }

    @Transactional
    public void finish(Long orderId, Long workerUserId, String note) {
        RepairOrder o = requireOrder(orderId);
        if (o.getAssignedWorkerUserId() == null || !o.getAssignedWorkerUserId().equals(workerUserId)) {
            throw new IllegalArgumentException("非当前工单维修工");
        }
        if (!"IN_PROGRESS".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalArgumentException("仅处理中工单可完成");
        }
        String from = o.getStatus();
        o.setStatus("FINISHED");
        o.setFinishedAt(LocalDateTime.now());
        repairOrderMapper.updateById(o);
        appendLog(orderId, "FINISH", from, "FINISHED", note, workerUserId);
    }

    @Transactional
    public void close(Long orderId, Long operatorUserId, String note) {
        RepairOrder o = requireOrder(orderId);
        if (!"FINISHED".equalsIgnoreCase(o.getStatus()) && !"ASSIGNED".equalsIgnoreCase(o.getStatus()) && !"IN_PROGRESS".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalArgumentException("当前状态不可关闭");
        }
        String from = o.getStatus();
        o.setStatus("CLOSED");
        repairOrderMapper.updateById(o);
        appendLog(orderId, "CLOSE", from, "CLOSED", note, operatorUserId);
    }

    @Transactional
    public void rate(Long orderId, Long studentId, Long operatorUserId, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分范围1-5");
        }
        RepairOrder o = requireOrder(orderId);
        if (!o.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("非本人报修单");
        }
        o.setRating(rating);
        repairOrderMapper.updateById(o);
        appendLog(orderId, "RATE", o.getStatus(), o.getStatus(), "评分: " + rating, operatorUserId);
    }

    private RepairOrder requireOrder(Long orderId) {
        RepairOrder o = repairOrderMapper.selectById(orderId);
        if (o == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        return o;
    }

    private void appendLog(Long orderId, String action, String fromStatus, String toStatus, String note, Long operatorUserId) {
        RepairLog log = new RepairLog();
        log.setRepairOrderId(orderId);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setNote(note);
        log.setOperatorUserId(operatorUserId);
        repairLogMapper.insert(log);
    }
}
