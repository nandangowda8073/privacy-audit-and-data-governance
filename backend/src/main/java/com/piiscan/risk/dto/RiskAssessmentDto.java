package com.piiscan.risk.dto;

import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RiskAssessmentDto {
    private Long id;
    private Long piiEntityId;
    private PiiEntityType piiType;
    private RiskLevel riskLevel;
    private Integer riskScore;
    private String reason;
    private String source; // Derived from PiiEntity
    private String columnName; // Derived from PiiEntity
    private LocalDateTime createdAt;
}
