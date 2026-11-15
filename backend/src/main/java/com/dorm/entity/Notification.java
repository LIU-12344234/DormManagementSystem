package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String targetType;

    private String targetValue;

    private Long publisherUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime publishAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
