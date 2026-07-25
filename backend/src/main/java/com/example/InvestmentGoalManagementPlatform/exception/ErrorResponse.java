package com.example.InvestmentGoalManagementPlatform.exception;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String reason;
    private String message;
    private String path;

    public ErrorResponse(
            int status,
            String reason,
            String message,
            String path
    ){
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.path = path;
    }
}