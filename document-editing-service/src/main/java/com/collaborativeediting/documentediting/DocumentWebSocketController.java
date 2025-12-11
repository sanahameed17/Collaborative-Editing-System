package com.collaborativeediting.documentediting;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class DocumentWebSocketController {

    @MessageMapping("/edit")
    @SendTo("/topic/document")
    public String handleEdit(String message) {
        // Process the edit and broadcast to subscribers
        return message;
    }
}
