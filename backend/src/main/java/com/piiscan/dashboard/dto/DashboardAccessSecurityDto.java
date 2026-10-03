package com.piiscan.dashboard.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAccessSecurityDto {
    private long totalAccessAttempts;
    private long accessDeniedCount;
    private double accessDeniedRate;
}
