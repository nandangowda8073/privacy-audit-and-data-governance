package com.piiscan.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {
    // 8 KPI Counters
    private long totalDatabases;
    private long totalTables;
    private long totalRecords;
    private long recordsScanned;
    private long piiDetected;
    private long highRiskRecords;
    private long policyViolations;
    private long activeUsers;

    // Summary & Status Widgets
    private double complianceScore;       // Derived mathematically bounded [0, 100]
    private DashboardAccessSecurityDto accessSecurityMetrics; // Separate access metrics
    private int privacyRiskScore;        // e.g. 78 (Out of 100)
    private String systemHealth;         // e.g. "OPERATIONAL", "HEALTHY"
    private int connectedDatabases;      // e.g. 14
    private String scannerStatus;        // e.g. "IDLE", "SCANNING", "ACTIVE"
    private String authenticationStatus; // e.g. "JWT_SECURE", "ACTIVE"
    private String lastScan;             // e.g. "02-Aug-2026 02:00 AM"
    private String nextScan;             // e.g. "03-Aug-2026 02:00 AM"
}
