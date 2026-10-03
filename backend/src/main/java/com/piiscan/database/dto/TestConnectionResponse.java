package com.piiscan.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestConnectionResponse {
    private boolean success;
    private String message;
    private String databaseVersion;
    private long responseTimeMs;
    private LocalDateTime timestamp;
}
