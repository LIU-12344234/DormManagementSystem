package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("late_return_record")
public class LateReturnRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime lateTime;

    private String reason;

    private Long handledBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
