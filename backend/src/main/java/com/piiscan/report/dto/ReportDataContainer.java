package com.piiscan.report.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDataContainer {

    private String reportTitle;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String databaseName;
    private Long scanId;

    // Aggregates & Metrics
    private long totalRecordsScanned;
    private long totalTablesScanned;
    private long totalPiiFindings;
    private long lowRiskCount;
    private long mediumRiskCount;
    private long highRiskCount;
    private long criticalRiskCount;
    private double overallRiskScore;
    private double maskingCoveragePercentage;
    private long policyViolationsCount;
    private long accessDeniedEventsCount;

    // Detailed Table Rows
    @Builder.Default
    private List<Map<String, Object>> rows = new ArrayList<>();

    // Distribution Maps
    @Builder.Default
    private Map<String, Long> riskDistribution = new HashMap<>();

    @Builder.Default
    private Map<String, Long> piiTypeDistribution = new HashMap<>();

    @Builder.Default
    private Map<String, Long> databaseRiskDistribution = new HashMap<>();
}
