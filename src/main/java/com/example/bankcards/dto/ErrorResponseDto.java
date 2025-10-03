package com.example.bankcards.dto;

import java.time.LocalDateTime;

public class ErrorResponseDto {
    private int statusCode;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponseDto(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}