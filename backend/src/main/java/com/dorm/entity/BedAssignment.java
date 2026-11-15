package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bed_assignment")
public class BedAssignment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long bedId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime checkInAt;

    private LocalDateTime checkOutAt;

    private String reason;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
