package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visitor_record")
public class VisitorRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long buildingId;

    private Long roomId;

    private Long visitedStudentId;

    private String visitorName;

    private String visitorPhone;

    private String visitorIdCard;

    private String relationDesc;

    private String visitReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime inTime;

    private LocalDateTime outTime;

    private Long registrarUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
