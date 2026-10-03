package com.piiscan.masking.service;

import com.piiscan.auth.entity.Role;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Secure audit logger for masking decisions.
 * IMPORTANT: Raw sensitive PII values are NEVER logged.
 * Only metadata (user, role, pii type, risk level, decision) is recorded.
 */
@Component
@Slf4j
public class MaskingAuditLogger {

    @org.springframework.beans.factory.annotation.Autowired
    private com.piiscan.audit.service.AuditLogService auditLogService;

    public void log(String username, Role role, PiiEntityType piiType,
                    RiskLevel riskLevel, MaskingType decision, String reason) {
        log.info("[MASKING_AUDIT] User: [{}] | Role: [{}] | PII: [{}] | Risk: [{}] | Decision: [{}] | Reason: [{}]",
                username,
                role != null ? role.name() : "UNKNOWN",
                piiType,
                riskLevel,
                decision,
                reason);
        if (auditLogService != null) {
            auditLogService.logMaskingEvent(
                com.piiscan.audit.enums.AuditAction.MASKING_APPLIED,
                com.piiscan.audit.enums.AuditResult.SUCCESS,
                username,
                role != null ? role.name() : "UNKNOWN",
                piiType != null ? piiType.name() : "UNKNOWN",
                riskLevel != null ? riskLevel.name() : "UNKNOWN",
                decision != null ? decision.name() : "UNKNOWN",
                reason
            );
        }
    }

    public void logPolicyChange(String username, String action, Long policyId) {
        log.info("[POLICY_AUDIT] User: [{}] | Action: [{}] | PolicyId: [{}]",
                username, action, policyId);
        if (auditLogService != null) {
            com.piiscan.audit.enums.AuditAction auditAction = switch (action) {
                case "POLICY_CREATED" -> com.piiscan.audit.enums.AuditAction.MASKING_POLICY_CREATED;
                case "POLICY_UPDATED" -> com.piiscan.audit.enums.AuditAction.MASKING_POLICY_UPDATED;
                case "POLICY_DELETED" -> com.piiscan.audit.enums.AuditAction.MASKING_POLICY_DELETED;
                default -> com.piiscan.audit.enums.AuditAction.MASKING_POLICY_UPDATED;
            };
            auditLogService.logSecurityEvent(
                auditAction,
                com.piiscan.audit.enums.AuditResult.SUCCESS,
                username,
                "MASKING_POLICY",
                String.valueOf(policyId),
                "Masking policy " + action + " (ID: " + policyId + ")",
                "Policy configuration modified by admin",
                "MASKING_POLICY_UPDATE"
            );
        }
    }
}
