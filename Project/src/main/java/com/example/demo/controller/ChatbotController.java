package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /** Send a message to VaultBot (Grok-powered chatbot) */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> message(@RequestBody ChatMessageDto request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }
        return ResponseEntity.ok(chatbotService.chat(request));
    }

    /** Health check — confirms chatbot connectivity */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "operational",
            "service", "VaultBot — Safe Deposit Locker Assistant",
            "model", "Grok (xAI)"
        ));
    }
}
