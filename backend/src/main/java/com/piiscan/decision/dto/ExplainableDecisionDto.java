package com.piiscan.decision.dto;

import com.piiscan.decision.enums.DecisionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplainableDecisionDto {
    private String decisionId;
    private DecisionType decisionType;
    private String outcome;               // e.g. "CRITICAL", "FULL", "ALLOWED", "DENIED", "HISTORICAL_EXPLANATION_UNAVAILABLE"
    private boolean isHistorical;
    private Long userId;
    private String username;
    private String role;
    private Long piiEntityId;
    private String piiType;
    private String riskLevel;
    private Integer riskScore;
    private String requiredPermission;
    private Boolean permissionGranted;
    private String maskingType;
    private Long policyId;
    private String policyName;
    private String policyRule;
    private String reason;                // Human-readable explanation
    private List<String> factors;         // Structured deterministic factors
    private List<String> decisionTrace;   // Step-by-step decision flow
    private LocalDateTime timestamp;
    private String correlationId;
    private String resourceType;
    private String resourceId;
}
