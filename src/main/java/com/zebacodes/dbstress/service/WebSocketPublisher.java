package com.zebacodes.dbstress.service;

import com.zebacodes.dbstress.model.LiveMetrics;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(UUID testId, LiveMetrics metrics) {
        messagingTemplate.convertAndSend("/topic/metrics/" + testId, metrics);
    }
}
