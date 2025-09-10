package com.example.demo.request;

import com.example.demo.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MessageRequest extends DefaultRequest {

    private String content;
    private UUID senderId;
    private UUID receiverId;

    private UUID chatId;
    private MessageType type;

}
