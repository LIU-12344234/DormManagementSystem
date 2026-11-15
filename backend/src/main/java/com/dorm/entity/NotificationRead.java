package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification_read")
public class NotificationRead {
    private Long notificationId;
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime readAt;
}
