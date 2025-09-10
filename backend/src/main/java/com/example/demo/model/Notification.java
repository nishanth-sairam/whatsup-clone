package com.example.demo.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    private UUID chatId;
    private String content;
    private UUID senderId;
    private UUID receiverId;
    private String chatName;
    private MessageType messageType;
    private NotificationType notificationType;
    private byte[] media;

}
