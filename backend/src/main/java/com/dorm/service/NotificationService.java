package com.dorm.service;

import com.dorm.mapper.NotificationReadMapper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationReadMapper notificationReadMapper;

    public NotificationService(NotificationReadMapper notificationReadMapper) {
        this.notificationReadMapper = notificationReadMapper;
    }

    public void markRead(Long notificationId, Long userId) {
        notificationReadMapper.insertIgnore(notificationId, userId);
    }

    public boolean isRead(Long notificationId, Long userId) {
        return notificationReadMapper.exists(notificationId, userId) != null;
    }
}
