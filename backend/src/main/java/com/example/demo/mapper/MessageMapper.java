package com.example.demo.mapper;

import com.example.demo.model.Message;
import com.example.demo.response.MessageResponse;
import com.example.demo.util.FileUtil;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@Service
public class MessageMapper {
    public MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .messageId(message.getId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .type(message.getType())
                .state(message.getState())
                .createdAt(message.getCreatedAt())
                .media(FileUtil.readFileFromLocation((message.getMediaFilePath())))
                .build();
    }
}
