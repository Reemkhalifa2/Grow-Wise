package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {


    private final AiService aiService;


    public AiController(AiService aiService){
        this.aiService = aiService;
    }


    @PostMapping("/ask")
    public String ask(@RequestBody String question){

        return aiService.askAI(question);
    }

}