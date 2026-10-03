package com.piiscan.report.dto;

import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.enums.ReportType;
import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportPreviewResponse {
    private ReportType reportType;
    private ReportFormat format;
    private String title;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private long totalRecordsScanned;
    private long totalFindings;
    private Map<String, Long> riskDistribution;
    private String estimatedSize;
    private String description;
}
