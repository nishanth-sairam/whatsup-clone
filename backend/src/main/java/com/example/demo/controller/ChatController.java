package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chats")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    /**
     * Temporary API endpoint for Keycloak backend server check
     * Returns a simple OK response to verify the backend is running
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAllChats(Authentication authentication) {
        try {
            logger.info("Chat API called - Backend server is running");

            // Create a simple response to verify backend connectivity
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("message", "Backend server is running and accessible");
            response.put("timestamp", LocalDateTime.now());
            response.put("endpoint", "/api/v1/chats");

            // If authentication is available, include user info
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                response.put("authenticatedUser", jwt.getClaimAsString("preferred_username"));
            } else {
                response.put("authenticatedUser", "anonymous");
            }

            // Mock chat data for testing purposes
            List<Map<String, Object>> mockChats = Arrays.asList(
                    createMockChat(1L, "General Chat", "Welcome to the chat!", LocalDateTime.now().minusHours(1)),
                    createMockChat(2L, "Random", "Random discussions here", LocalDateTime.now().minusMinutes(30)),
                    createMockChat(3L, "Tech Talk", "Discuss technology", LocalDateTime.now().minusMinutes(5)));

            response.put("chats", mockChats);
            response.put("totalChats", mockChats.size());

            logger.info("Returning mock chat data for testing");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in chat endpoint", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "An error occurred while processing the request");
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check endpoint specifically for Keycloak integration testing
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Chat Service");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0-SNAPSHOT");

        logger.info("Health check endpoint called");
        return ResponseEntity.ok(health);
    }

    /**
     * Simple ping endpoint for connectivity testing
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now().toString());

        logger.info("Ping endpoint called");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create mock chat data
     */
    private Map<String, Object> createMockChat(Long id, String name, String description, LocalDateTime lastActivity) {
        Map<String, Object> chat = new HashMap<>();
        chat.put("id", id);
        chat.put("name", name);
        chat.put("description", description);
        chat.put("lastActivity", lastActivity);
        chat.put("participantCount", (int) (Math.random() * 10) + 1);
        chat.put("unreadCount", (int) (Math.random() * 5));
        chat.put("isActive", true);
        return chat;
    }
}
