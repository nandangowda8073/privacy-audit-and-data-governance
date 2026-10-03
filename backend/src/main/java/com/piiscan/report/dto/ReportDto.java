package com.piiscan.report.dto;

import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.enums.ReportStatus;
import com.piiscan.report.enums.ReportType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {
    private Long id;
    private ReportType reportType;
    private ReportFormat format;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long databaseId;
    private Long scanId;
    private ReportStatus status;
    private String fileName;
    private Long fileSize;
    private String description;
}
