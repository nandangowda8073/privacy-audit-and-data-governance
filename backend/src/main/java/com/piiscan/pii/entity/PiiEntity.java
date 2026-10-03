package com.piiscan.pii.entity;

import com.piiscan.pii.enums.PiiCategory;
import com.piiscan.pii.enums.DetectionMethod;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pii_entities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PiiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PiiEntityType entityType;

    @Enumerated(EnumType.STRING)
    private PiiCategory category;

    @Column(length = 1000)
    private String originalValue;

    @Column(length = 1000)
    private String maskedValue;

    private Double confidenceScore;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    private DetectionMethod detectionMethod;

    private String source;        // "TEXT", "FILE:filename.csv", "DB:tableName"
    private String columnName;    // relevant for DB scans
    private Integer startIndex;
    private Integer endIndex;

    private String detectedBy;    // username or "SYSTEM"

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
