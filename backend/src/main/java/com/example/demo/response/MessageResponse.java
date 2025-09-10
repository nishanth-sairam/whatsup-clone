package com.example.demo.response;


import com.example.demo.model.MessageState;
import com.example.demo.model.MessageType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private UUID messageId;
    private String content;
    private MessageType type;
    private MessageState state;
    private byte[] media;
    private UUID senderId;
    private UUID receiverId;
    private LocalDateTime createdAt;
}
