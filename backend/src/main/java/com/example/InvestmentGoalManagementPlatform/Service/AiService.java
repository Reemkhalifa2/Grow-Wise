package com.example.InvestmentGoalManagementPlatform.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String askAI(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}