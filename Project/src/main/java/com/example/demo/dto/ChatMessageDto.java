package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public class ChatMessageDto {
    private String message;
    /** Optional: conversation history [{role: "user"|"assistant", content: "..."}] */
    private List<Map<String, String>> history;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<Map<String, String>> getHistory() { return history; }
    public void setHistory(List<Map<String, String>> history) { this.history = history; }
}
