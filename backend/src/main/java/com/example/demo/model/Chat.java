package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chats")
public class Chat extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
    @OrderBy("createdAt DESC")
    @JsonManagedReference
    private List<Message> messages;

    @Transient
    public String getChatName(String senderId) {
        return sender.getId().toString().equals(senderId) ? receiver.getFirstName() + " " + receiver.getLastName() : sender.getFirstName() + " " + sender.getLastName();
    }

    @Transient
    public Long getUnreadMessagesCount(final String userId) {
        return messages.stream()
                .filter(msg -> msg.getReceiverId().toString().equals(userId))
                .filter(msg -> msg.getState() == MessageState.SENT)
                .count();
    }

    @Transient
    public String getLastMessage() {
        if (messages != null && !messages.isEmpty()) {
            if (messages.get(0).getType() != MessageType.TEXT) {
                return "Attachment";
            }
            return messages.get(0).getContent();
        }
        return null;
    }

    @Transient
    public LocalDateTime getLastMessageTime() {
        if (messages != null && !messages.isEmpty()) {
            return messages.get(0).getCreatedAt();
        }
        return null;
    }
}
