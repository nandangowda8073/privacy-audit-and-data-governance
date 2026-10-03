package com.piiscan.report.dto;

import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.enums.ReportType;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportGenerateRequest {
    private ReportType reportType;
    private ReportFormat format;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long databaseId;
    private Long scanId;
    private RiskLevel riskLevel;
    private PiiEntityType piiType;
    private String username;
    private String action;
    private String result;
}
