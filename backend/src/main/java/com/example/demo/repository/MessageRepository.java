package com.example.demo.repository;


import com.example.demo.model.Message;
import com.example.demo.model.MessageState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query(value = "SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt")
    List<Message> findMessagesByChatId(UUID chatId);

    Page<Message> findAllByChatIdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    @Query(value = "UPDATE Message m SET m.state = :newState WHERE m.chat.id = :chatId")
    @Modifying
    void setMessageToSeenByChatId(UUID chatId, MessageState newState);
}
