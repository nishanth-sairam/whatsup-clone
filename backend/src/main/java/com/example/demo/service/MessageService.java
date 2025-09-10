package com.example.demo.service;


import com.example.demo.mapper.MessageMapper;
import com.example.demo.model.*;
import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.request.MessageRequest;
import com.example.demo.response.MessageResponse;
import com.example.demo.util.FileUtil;
import com.example.demo.util.PageUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper mapper;
    private final FileService fileService;
    private final NotificationService notificationService;

    public Message saveMessage(MessageRequest messageRequest) {

        Chat chat = getChatById(messageRequest.getChatId());

        Message newMessage = new Message();
        newMessage.setContent(messageRequest.getContent());
        newMessage.setSenderId(messageRequest.getSenderId());
        newMessage.setReceiverId(messageRequest.getReceiverId());
        newMessage.setChat(chat);
        newMessage.setType(messageRequest.getType());
        newMessage.setState(MessageState.SENT);

        Message message = messageRepository.save(newMessage);


        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(messageRequest.getType())
                .content(messageRequest.getContent())
                .senderId(messageRequest.getSenderId())
                .receiverId(messageRequest.getReceiverId())
                .notificationType(NotificationType.MESSAGE)
                .chatName(chat.getChatName(newMessage.getSenderId().toString()))
                .build();

        notificationService.sendNotification(newMessage.getReceiverId(), notification);
        return message;

    }


    public List<MessageResponse> findChatMessages(UUID chatId) {
        return messageRepository.findMessagesByChatId(chatId).stream().map(mapper::toMessageResponse).toList();
    }

    public Page<MessageResponse> findChatMessages(UUID chatId, Pageable pageable) {
        return messageRepository.findAllByChatIdOrderByCreatedAtDesc(chatId, pageable).map(mapper::toMessageResponse);
    }

    @Transactional
    public void setMessageToSeen(UUID chatId, Authentication authentication) {
        Chat chat = getChatById(chatId);

        final UUID receiverId = getReceiverId(chat, authentication);

        messageRepository.setMessageToSeenByChatId(chatId, MessageState.SEEN);
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .senderId(getSenderId(chat, authentication))
                .receiverId(getReceiverId(chat, authentication))
                .notificationType(NotificationType.SEEN)
                .build();

        notificationService.sendNotification(receiverId, notification);

    }

    public void uploadMediaMessage(UUID chatId, MultipartFile file, Authentication authentication) throws IOException {
        Chat chat = getChatById(chatId);

        final UUID senderId = getSenderId(chat, authentication);
        final UUID receiverId = getReceiverId(chat, authentication);

        final String filePath = fileService.saveFile(file, senderId);

        Message newMessage = new Message();
        newMessage.setSenderId(senderId);
        newMessage.setReceiverId(receiverId);
        newMessage.setChat(chat);
        newMessage.setType(MessageType.IMAGE);
        newMessage.setState(MessageState.SENT);
        newMessage.setMediaFilePath(filePath);
        messageRepository.save(newMessage);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(MessageType.IMAGE)
                .notificationType(NotificationType.IMAGE)
                .senderId(senderId)
                .receiverId(receiverId)
                .media(FileUtil.readFileFromLocation(filePath))
                .build();

        notificationService.sendNotification(receiverId, notification);

    }

    private UUID getSenderId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().toString().equals(authentication.getName())) {
            return chat.getReceiver().getId();
        }
        return chat.getSender().getId();
    }

    private UUID getReceiverId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().toString().equals(authentication.getName())) {
            return chat.getReceiver().getId();
        }
        return chat.getSender().getId();
    }

    private Chat getChatById(UUID chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException("Chat not found with ID: " + chatId));
    }


    public Page<MessageResponse> findChatMessages(MessageRequest messageRequest) {
        Pageable pageable = PageUtil.getPageable(
                messageRequest.getPage(),
                messageRequest.getSize(),
                messageRequest.getSortBy(),
                messageRequest.getSortDir(),
                "createdAt",
                "desc"
        );
        Page<Message> pageMessages = messageRepository.findAllByChatIdOrderByCreatedAtDesc(messageRequest.getChatId(), pageable);
        List<MessageResponse> messageResponses = pageMessages.getContent().stream().map(mapper::toMessageResponse).toList();
        Page<MessageResponse> responsePage = new PageImpl<>(messageResponses, pageable, pageMessages.getTotalElements());
        return responsePage;
    }


}
