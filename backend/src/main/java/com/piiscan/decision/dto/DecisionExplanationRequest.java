package com.piiscan.decision.dto;

import com.piiscan.auth.entity.Permission;
import com.piiscan.decision.enums.DecisionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionExplanationRequest {
    private DecisionType decisionType;
    private Long piiEntityId;
    private Long auditLogId;
    private Permission requiredPermission;
    private String targetUsername; // Admin can specify target user if authorized
}
