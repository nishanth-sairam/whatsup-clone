package com.example.demo.repository;

import com.example.demo.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query(value = "SELECT DISTINCT c FROM Chat c WHERE c.sender.id = :senderId OR c.receiver.id = :senderId ORDER BY c.createdAt DESC")
    List<Chat> findChatsByUserId(UUID senderId);

    @Query(value = "SELECT DISTINCT c FROM Chat c WHERE (c.sender.id = :senderId AND c.receiver.id = :receiverId) OR (c.sender.id = :receiverId AND c.receiver.id = :senderId)")
    Optional<Chat> finChatByReceiverAndSender(UUID senderId, UUID receiverId);
}
