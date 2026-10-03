package com.piiscan.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDto {
    private String id;
    private String time;
    private String user;
    private String userRole;
    private String action;
    private String status; // "COMPLETED", "SUCCESS", "FAILED", "WARNING", "IN_PROGRESS"
}
