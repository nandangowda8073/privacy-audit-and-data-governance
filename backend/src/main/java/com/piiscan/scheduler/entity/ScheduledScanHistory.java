package com.piiscan.scheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_scan_history")
@Data
public class ScheduledScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ScanType scanType;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private ScanStatus status;

    private Long duration; // in seconds

    private Integer databasesScanned;
    private Integer tablesScanned;
    private Long recordsScanned;
    private Long piiDetected;

    @Column(length = 1000)
    private String failedReason;

    private Integer retryCount;
    private String triggeredBy;
}
