// src/main/java/com/libraryservice/notification/NotificationService.java
package org.example.libraryservice.notification;

import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a message to a specific user's private queue.
     * Clients should subscribe to /user/queue/{topic}
     * (e.g., /user/queue/rental-issued)
     */
    public void sendToUser(User user, String topic, Object payload) {
        // The SimpMessagingTemplate resolves the user's email to their session
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),  // The user's name (which is their email in UserDetails)
                "/queue/" + topic, // The destination
                payload           // The data
        );
    }

    /**
     * Sends a message to a public topic.
     * Clients should subscribe to /topic/{topic}
     * (e.g., /topic/book-added)
     */
    public void sendToAll(String topic, Object payload) {
        messagingTemplate.convertAndSend("/topic/" + topic, payload);
    }
}