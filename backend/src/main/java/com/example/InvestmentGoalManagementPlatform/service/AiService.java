package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.exception.AiException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;


    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }


    public String askAI(String question) {

        try {

            return chatClient
                    .prompt(question)
                    .call()
                    .content();

        } catch (Exception e) {

            throw new AiException(
                    "AI service failed: " + e.getMessage()
            );
        }
    }
}

