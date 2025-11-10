package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.request.ChatRequest;
import com.example.demo.response.ChatResponse;
import com.example.demo.response.StringResponse;
import com.example.demo.service.ChatService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<StringResponse> createChat(@RequestParam() String senderId, @RequestParam() String receiverId) throws Exception {
        final String chatId = chatService.createChat(senderId, receiverId).toString();
        return ResponseEntity.ok(StringResponse.builder().response(chatId).build());
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getChatsByReceiver(ChatRequest request) {
        return ResponseEntity.ok(chatService.getChatsByReceiverId(request));
    }
}
