package com.piiscan.pii.dto;

import com.piiscan.pii.enums.DetectionMethod;
import com.piiscan.pii.enums.PiiCategory;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a single PII match found during detection.
 * Used internally by detectors before persistence.
 */
@Data
@Builder
public class DetectionMatch {
    private PiiEntityType entityType;
    private PiiCategory   category;
    private RiskLevel     riskLevel;
    private DetectionMethod detectionMethod;
    private String        originalValue;
    private String        maskedValue;
    private double        confidenceScore;
    private int           startIndex;
    private int           endIndex;
}
