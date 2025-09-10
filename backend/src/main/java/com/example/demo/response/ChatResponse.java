package com.example.demo.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
public class ChatResponse {

    private String id;
    private String name;
    private Long unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean isReceiverOnline;
    private UUID senderId;
    private UUID receiverId;

}
