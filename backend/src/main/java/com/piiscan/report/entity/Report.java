package com.piiscan.report.entity;

import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.enums.ReportStatus;
import com.piiscan.report.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports", indexes = {
    @Index(name = "idx_reports_type", columnList = "report_type"),
    @Index(name = "idx_reports_generated_by", columnList = "generated_by"),
    @Index(name = "idx_reports_generated_at", columnList = "generated_at"),
    @Index(name = "idx_reports_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private ReportFormat format;

    @Column(name = "generated_by", nullable = false)
    private String generatedBy;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "database_id")
    private Long databaseId;

    @Column(name = "scan_id")
    private Long scanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "description", length = 500)
    private String description;
}
