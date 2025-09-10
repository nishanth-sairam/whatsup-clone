package com.example.demo.mapper;

import com.example.demo.model.Chat;
import com.example.demo.response.ChatResponse;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@Service
public class ChatMapper {
    public ChatResponse toChatResponse(Chat chat, String senderId) {
        ChatResponse chatResponse = ChatResponse.builder()
                .id(String.valueOf(chat.getId()))
                .name(chat.getChatName(senderId))
                .unreadCount(chat.getUnreadMessagesCount(senderId))
                .lastMessage(chat.getLastMessage())
                .isReceiverOnline(chat.getReceiver().isUserOnline())
                .senderId(chat.getSender().getId())
                .receiverId(chat.getReceiver().getId())
                .lastMessageTime(chat.getLastMessageTime())
                .build();
        return chatResponse;
    }


}
