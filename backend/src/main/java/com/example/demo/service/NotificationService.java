package com.example.demo.service;

import com.example.demo.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendNotification(UUID userId, Notification notification) {
        log.info("Sending notification to user: {} with payload: {}", userId, notification);
        simpMessagingTemplate.convertAndSendToUser(userId.toString(), "/chat", notification);
    }
}
