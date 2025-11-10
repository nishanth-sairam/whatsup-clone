package com.example.demo.repository;

import com.example.demo.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID>, JpaSpecificationExecutor<Chat> {

    @Query(value = "SELECT DISTINCT c FROM Chat c WHERE c.sender.id = ?1 OR c.receiver.id = ?1 ORDER BY c.createdAt DESC")
    List<Chat> findChatsByUserId(UUID senderId);

    @Query(value = "SELECT DISTINCT c FROM Chat c WHERE (c.sender.id = ?1 AND c.receiver.id = ?2) OR (c.sender.id = ?2 AND c.receiver.id = ?1)")
    Optional<Chat> finChatByReceiverAndSender(UUID senderId, UUID receiverId);
}
