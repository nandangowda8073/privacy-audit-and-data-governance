package com.piiscan.pii.dto;

import com.piiscan.pii.enums.DetectionMethod;
import com.piiscan.pii.enums.PiiCategory;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** API-facing representation of a persisted PiiEntity */
@Data
@Builder
public class PiiEntityDto {
    private Long id;
    private PiiEntityType entityType;
    private PiiCategory category;
    private String originalValue;
    private String maskedValue;
    private Double confidenceScore;
    private RiskLevel riskLevel;
    private DetectionMethod detectionMethod;
    private String source;
    private String columnName;
    private Integer startIndex;
    private Integer endIndex;
    private String detectedBy;
    private LocalDateTime createdAt;
}
