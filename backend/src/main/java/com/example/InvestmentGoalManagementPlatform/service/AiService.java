package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.AiAllocationSuggestionDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;


    public AiService(ChatClient.Builder builder,
                     ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public AiAllocationSuggestionDTO generateAllocationSuggestion(
            String prompt,
            Integer goalId,
            Double monthlyInvestmentAmount) {

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        try {
            return objectMapper.readValue(
                    response,
                    AiAllocationSuggestionDTO.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid AI response", e);
        }
    }
}