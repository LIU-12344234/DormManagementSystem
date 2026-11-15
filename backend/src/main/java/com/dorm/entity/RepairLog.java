package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_log")
public class RepairLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repairOrderId;

    private String action;

    private String fromStatus;

    private String toStatus;

    private String note;

    private Long operatorUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
