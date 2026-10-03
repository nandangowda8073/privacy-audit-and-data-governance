package com.piiscan.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultDto {
    private String id;
    private String database;
    private int tablesScanned;
    private long recordsScanned;
    private long piiFound;
    private String riskLevel; // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    private String status;    // "COMPLETED", "IN_PROGRESS", "FAILED"
    private String timestamp;
}
