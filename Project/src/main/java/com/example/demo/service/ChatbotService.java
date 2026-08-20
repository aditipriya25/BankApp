package com.example.demo.service;

import com.example.demo.dto.ChatMessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Chatbot service powered by Grok (xAI) API.
 * Endpoint: https://api.x.ai/v1/chat/completions
 * Model: grok-3-mini (fast, cost-effective)
 *
 * The chatbot is specialized as a VaultBank locker assistant that:
 *  - Answers questions about RBI locker guidelines
 *  - Helps customers understand nomination, closure, rent procedures
 *  - Provides guidance on KYC, agreements, and access procedures
 */
@Service
public class ChatbotService {

    @Value("${grok.api.key:}")
    private String grokApiKey;

    @Value("${grok.api.url:https://api.x.ai/v1/chat/completions}")
    private String grokApiUrl;

    @Value("${grok.model:grok-3-mini}")
    private String grokModel;

    private static final String SYSTEM_PROMPT =
        "You are VaultBot, an expert AI assistant for VaultBank Safe Deposit Locker Services. " +
        "You specialize in RBI locker guidelines per circular DOR.LEG.REC/40/09.07.005/2021-22. " +
        "You help customers with: locker allotment, nomination (Forms SL1/SL1A/SL2/SL3), " +
        "agreements, rent payment (annual, via UPI/Card/NetBanking), locker access, " +
        "closure procedures (normal surrender, death claims within 15 days, " +
        "non-payment forced closure after 3 years, inoperative locker after 7 years), " +
        "and compensation (100x annual rent for bank negligence). " +
        "Always be professional, clear, and cite relevant RBI guideline paragraphs when applicable. " +
        "Keep responses concise and helpful. If asked something outside banking/locker scope, " +
        "politely redirect to locker services.";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send a message to Grok API and get a response.
     * Falls back to a helpful canned response if the API key is not configured.
     */
    public Map<String, Object> chat(ChatMessageDto request) {
        if (grokApiKey == null || grokApiKey.isBlank()) {
            return getFallbackResponse(request.getMessage());
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(grokApiKey);

            // Build messages array with system prompt + history + new message
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

            // Add conversation history if provided
            if (request.getHistory() != null) {
                for (Map<String, String> histMsg : request.getHistory()) {
                    String role    = histMsg.getOrDefault("role", "user");
                    String content = histMsg.getOrDefault("content", "");
                    if (!content.isBlank() && (role.equals("user") || role.equals("assistant"))) {
                        messages.add(Map.of("role", role, "content", content));
                    }
                }
            }

            messages.add(Map.of("role", "user", "content", request.getMessage()));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", grokModel);
            body.put("messages", messages);
            body.put("max_tokens", 512);
            body.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(grokApiUrl, entity, Map.class);

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String assistantReply = (String) message.get("content");
                    return Map.of(
                        "reply", assistantReply,
                        "model", grokModel,
                        "status", "success"
                    );
                }
            }
        } catch (Exception e) {
            // Fall back gracefully
            return getFallbackResponse(request.getMessage());
        }

        return getFallbackResponse(request.getMessage());
    }

    /**
     * Fallback response when Grok API is unavailable or key not configured.
     * Provides rule-based locker guidance.
     */
    private Map<String, Object> getFallbackResponse(String message) {
        String msg = message.toLowerCase();
        String reply;

        if (msg.contains("nominee") || msg.contains("nomination")) {
            reply = "📋 **Nomination Facility**\n\nAs per RBI para 5.1, you can nominate a person for your locker using Forms SL1 (single hirer) or SL1A (joint hirer). For minor nominees, a guardian must be specified. To add a nominee, go to your dashboard → select your locker → click 'Add Nominee'.";
        } else if (msg.contains("close") || msg.contains("closure") || msg.contains("surrender")) {
            reply = "🔒 **Locker Closure Options**\n\n1. **Normal Closure**: You can surrender your locker anytime. Contact the bank with written authorization.\n2. **After Death**: Nominee can claim contents within 15 days (RBI para 5.2.4).\n3. **Non-Payment**: Bank may close locker if rent is unpaid for 3 consecutive years (RBI para 6.3.1).\n4. **Inoperative**: If unused for 7 years, bank may transfer contents to nominee (RBI para 6.4).";
        } else if (msg.contains("rent") || msg.contains("payment") || msg.contains("pay")) {
            reply = "💳 **Rent Payment**\n\nLocker rent is charged annually. You can pay via:\n• **UPI** (instant transfer)\n• **Card** (Debit/Credit)\n• **Net Banking**\n• **Offline** (at branch)\n\nIf rent is unpaid for 3+ consecutive years, the bank may initiate forced closure (RBI 6.3.1). Go to 'Pay Rent' in your dashboard.";
        } else if (msg.contains("agreement") || msg.contains("sign")) {
            reply = "📄 **Locker Agreement**\n\nAs per RBI para 2.1.2, a stamped agreement is required before locker allotment. You'll receive a copy of the agreement. Banks must renew agreements with existing customers by January 1, 2023. Go to 'My Agreement' in your dashboard to view and sign.";
        } else if (msg.contains("kyc") || msg.contains("aadhaar") || msg.contains("pan")) {
            reply = "🛡️ **KYC Verification**\n\nKYC is mandatory before locker allotment (RBI para 1.1). You need to submit Aadhaar and PAN details. Complete your KYC under 'Identity Verification' in the menu.";
        } else if (msg.contains("key") || msg.contains("lost")) {
            reply = "🔑 **Lost Key Procedure** (RBI para 6.1.1)\n\n1. Notify the bank immediately in writing\n2. Give an undertaking that if key is found, it will be returned to the bank\n3. Charges for opening, changing lock, and replacing key will be recovered from you\n4. The locker will be opened only in your presence by an authorized bank technician";
        } else if (msg.contains("compensation") || msg.contains("theft") || msg.contains("fire") || msg.contains("robbery")) {
            reply = "⚖️ **Bank Liability** (RBI para 7.2)\n\nFor events like fire, theft, burglary, robbery, building collapse, or fraud by bank employees, the bank's liability is **100 times the prevailing annual rent** of your locker. For natural calamities, the bank is not liable (RBI para 7.1).";
        } else if (msg.contains("visit") || msg.contains("access") || msg.contains("otp")) {
            reply = "📅 **Visiting Your Locker**\n\nTo access your locker:\n1. Book a visit slot in your dashboard\n2. You'll receive an OTP\n3. Show the OTP at the branch\n4. An SMS/email alert is sent after every locker access (RBI para 4.1.3)";
        } else {
            reply = "👋 Hello! I'm **VaultBot**, your locker assistant at VaultBank.\n\nI can help you with:\n• 📋 Nomination & forms (SL1/SL2/SL3)\n• 🔒 Locker closure procedures\n• 💳 Rent payment (UPI/Card/NetBanking)\n• 📄 Locker agreement\n• 🔑 Lost key procedures\n• 🛡️ KYC requirements\n• ⚖️ Bank liability & compensation\n• 📅 Visit booking\n\nHow can I assist you today?";
        }

        return Map.of(
            "reply", reply,
            "model", "fallback-rulebased",
            "status", "fallback"
        );
    }
}
