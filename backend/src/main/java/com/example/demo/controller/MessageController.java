package com.example.demo.controller;

import com.example.demo.constant.APIConstant;
import com.example.demo.model.Message;
import com.example.demo.request.MessageRequest;
import com.example.demo.response.MessageResponse;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping(APIConstant.API_V_1_MESSAGES)
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message saveMessage(@RequestBody MessageRequest messageRequest) {
        return messageService.saveMessage(messageRequest);
    }

    @PostMapping(value = APIConstant.UPLOAD_MEDIA, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> uploadMedia(@RequestParam() UUID chatId,
                                              @RequestParam() MultipartFile file, Authentication authentication) {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Check file size (additional check)
            if (file.getSize() > 100 * 1024 * 1024) { // 100MB
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File size exceeds maximum limit of 100MB");
            }

            messageService.uploadMediaMessage(chatId, file, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setMessageToSeen(@RequestParam() UUID chatId, Authentication authentication) {
        messageService.setMessageToSeen(chatId, authentication);
    }

    @GetMapping(APIConstant.CHATS + APIConstant.CHAT_ID_PATH)
    public ResponseEntity<Page<MessageResponse>> getMessages(@PathVariable UUID chatId, MessageRequest messageRequest) {
        return ResponseEntity.ok(messageService.findChatMessages(messageRequest));
    }

}
