package com.example.demo.service;

import com.example.demo.mapper.ChatMapper;
import com.example.demo.model.Chat;
import com.example.demo.model.User;
import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.request.ChatRequest;
import com.example.demo.response.ChatResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    private final ChatMapper mapper;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByReceiverId(ChatRequest chatRequest) {
        final UUID userId = chatRequest.getUser().getId();
        Specification<Chat> specification = (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.equal(root.get("sender").get("id"), userId),
                criteriaBuilder.equal(root.get("receiver").get("id"), userId));

        return chatRepository.findAll(specification).stream()
                .map(chat -> mapper.toChatResponse(chat, userId.toString())).toList();
    }

    public UUID createChat(String senderId, String receiverId) {
        Optional<Chat> existingChat = chatRepository.finChatByReceiverAndSender(UUID.fromString(senderId),
                UUID.fromString(receiverId));
        if (existingChat.isPresent()) {
            return existingChat.get().getId();
        }

        User sender = userRepository.findByPublicId(UUID.fromString(senderId))
                .orElseThrow(() -> new EntityNotFoundException("User not found with sender ID: " + senderId));

        User receiver = userRepository.findByPublicId(UUID.fromString(receiverId))
                .orElseThrow(() -> new EntityNotFoundException("User not found with receiver ID: " + receiverId));

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setReceiver(receiver);

        Chat saveChat = chatRepository.save(chat);

        return saveChat.getId();
    }

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

}
