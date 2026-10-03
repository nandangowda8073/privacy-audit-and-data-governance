package com.piiscan.decision.service.impl;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import com.piiscan.audit.repository.AuditLogRepository;
import com.piiscan.audit.service.AuditLogService;
import com.piiscan.auth.entity.Permission;
import com.piiscan.auth.entity.Role;
import com.piiscan.auth.entity.RolePermission;
import com.piiscan.auth.entity.User;
import com.piiscan.auth.repository.RolePermissionRepository;
import com.piiscan.auth.repository.UserRepository;
import com.piiscan.decision.dto.DecisionExplanationRequest;
import com.piiscan.decision.dto.ExplainableDecisionDto;
import com.piiscan.decision.enums.DecisionType;
import com.piiscan.decision.service.ExplainableDecisionService;
import com.piiscan.masking.entity.MaskingPolicy;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.masking.repository.MaskingPolicyRepository;
import com.piiscan.masking.service.MaskingPolicyService;
import com.piiscan.pii.entity.PiiEntity;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.pii.repository.PiiEntityRepository;
import com.piiscan.risk.entity.RiskAssessment;
import com.piiscan.risk.repository.RiskAssessmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExplainableDecisionServiceImpl implements ExplainableDecisionService {

    private final PiiEntityRepository piiEntityRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final MaskingPolicyService maskingPolicyService;
    private final MaskingPolicyRepository maskingPolicyRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ExplainableDecisionDto explainDecision(DecisionExplanationRequest request) {
        if (request == null || request.getDecisionType() == null) {
            throw new IllegalArgumentException("Decision type is required");
        }

        // 1. Resolve target user context securely from SecurityContext
        UserContext userCtx = resolveUserContext(request.getTargetUsername());

        ExplainableDecisionDto explanation;

        if (request.getAuditLogId() != null) {
            // Explain Historical Audit Event (NEVER recalculate using current policies!)
            explanation = explainHistoricalAuditLog(request.getAuditLogId());
        } else {
            // Explain Current Decision State
            switch (request.getDecisionType()) {
                case RISK_CLASSIFICATION:
                    explanation = explainCurrentRiskClassification(request.getPiiEntityId(), userCtx);
                    break;
                case MASKING:
                    explanation = explainCurrentMasking(request.getPiiEntityId(), userCtx);
                    break;
                case ACCESS:
                    explanation = explainCurrentAccess(request.getRequiredPermission(), userCtx);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported decision type: " + request.getDecisionType());
            }
        }

        // 2. Audit Log explicit decision explanation request
        auditLogService.logEvent(
                AuditAction.DECISION_EXPLAINED,
                AuditResult.SUCCESS,
                "EXPLAINABLE_DECISION",
                explanation.getDecisionId(),
                "Explained " + request.getDecisionType() + " decision for user: " + userCtx.username,
                null,
                request.getRequiredPermission() != null ? request.getRequiredPermission().name() : null,
                null
        );

        return explanation;
    }

    @Override
    @Transactional(readOnly = true)
    public ExplainableDecisionDto getDecisionById(String decisionId) {
        if (decisionId.startsWith("HIST-")) {
            Long logId = Long.parseLong(decisionId.replace("HIST-", ""));
            return explainHistoricalAuditLog(logId);
        } else if (decisionId.startsWith("DEC-RISK-")) {
            Long piiId = Long.parseLong(decisionId.replace("DEC-RISK-", ""));
            return explainCurrentRiskClassification(piiId, resolveUserContext(null));
        } else if (decisionId.startsWith("DEC-MASK-")) {
            Long piiId = Long.parseLong(decisionId.replace("DEC-MASK-", ""));
            return explainCurrentMasking(piiId, resolveUserContext(null));
        } else {
            throw new IllegalArgumentException("Invalid or unknown decision ID format: " + decisionId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExplainableDecisionDto> getDecisionHistory(Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        List<ExplainableDecisionDto> historyItems = logs.getContent().stream()
                .map(this::mapAuditLogToHistoricalDto)
                .collect(Collectors.toList());
        return new PageImpl<>(historyItems, pageable, logs.getTotalElements());
    }

    // ── Current Decision Explanations ──────────────────────────────────────────

    private ExplainableDecisionDto explainCurrentRiskClassification(Long piiEntityId, UserContext userCtx) {
        if (piiEntityId == null) {
            throw new IllegalArgumentException("piiEntityId is required for RISK_CLASSIFICATION explanation");
        }

        PiiEntity entity = piiEntityRepository.findById(piiEntityId)
                .orElseThrow(() -> new NoSuchElementException("PII entity not found with ID: " + piiEntityId));

        RiskAssessment assessment = riskAssessmentRepository.findAll().stream()
                .filter(r -> r.getPiiEntity() != null && r.getPiiEntity().getId().equals(piiEntityId))
                .findFirst()
                .orElse(null);

        PiiEntityType piiType = entity.getEntityType();
        RiskLevel riskLevel = assessment != null ? assessment.getRiskLevel() : entity.getRiskLevel();
        Integer riskScore = assessment != null ? assessment.getRiskScore() : (riskLevel == RiskLevel.CRITICAL ? 95 : (riskLevel == RiskLevel.HIGH ? 75 : 45));
        String reason = assessment != null ? assessment.getReason() : "Classified as " + riskLevel + " under Module 7 Risk Rules";

        List<String> factors = generateRiskFactors(piiType, riskLevel);

        List<String> trace = List.of(
                "Step 1: PII entity detected: " + piiType + " (Category: " + entity.getCategory() + ")",
                "Step 2: Risk Classification Engine evaluated rules for " + piiType,
                "Step 3: Assigned Risk Level: " + riskLevel + " with Score: " + riskScore + "/100",
                "Step 4: Classification Rationale: " + reason
        );

        return ExplainableDecisionDto.builder()
                .decisionId("DEC-RISK-" + piiEntityId)
                .decisionType(DecisionType.RISK_CLASSIFICATION)
                .outcome(riskLevel != null ? riskLevel.name() : "MEDIUM")
                .isHistorical(false)
                .userId(userCtx.userId)
                .username(userCtx.username)
                .role(userCtx.role.name())
                .piiEntityId(piiEntityId)
                .piiType(piiType != null ? piiType.name() : "UNKNOWN")
                .riskLevel(riskLevel != null ? riskLevel.name() : "MEDIUM")
                .riskScore(riskScore)
                .reason(reason)
                .factors(factors)
                .decisionTrace(trace)
                .timestamp(LocalDateTime.now())
                .resourceType("PII_ENTITY")
                .resourceId(String.valueOf(piiEntityId))
                .build();
    }

    private ExplainableDecisionDto explainCurrentMasking(Long piiEntityId, UserContext userCtx) {
        if (piiEntityId == null) {
            throw new IllegalArgumentException("piiEntityId is required for MASKING explanation");
        }

        PiiEntity entity = piiEntityRepository.findById(piiEntityId)
                .orElseThrow(() -> new NoSuchElementException("PII entity not found with ID: " + piiEntityId));

        PiiEntityType piiType = entity.getEntityType();
        RiskLevel riskLevel = entity.getRiskLevel() != null ? entity.getRiskLevel() : RiskLevel.MEDIUM;

        MaskingType maskingType = maskingPolicyService.resolve(userCtx.role, piiType, riskLevel);
        String reason = maskingPolicyService.generateReason(userCtx.role, piiType, riskLevel, maskingType);

        List<MaskingPolicy> policies = maskingPolicyRepository.findByRole(userCtx.role);
        Optional<MaskingPolicy> matchingPolicy = policies.stream()
                .filter(p -> (p.getPiiType() == null || p.getPiiType() == piiType) &&
                             (p.getRiskLevel() == null || p.getRiskLevel() == riskLevel))
                .findFirst();

        String policyName = matchingPolicy.map(p -> "Policy-" + p.getId() + " (" + p.getRole() + ")").orElse("Default Security Policy");

        List<String> factors = List.of(
                "User Role: " + userCtx.role,
                "Target PII Sensitivity: " + piiType + " (" + riskLevel + ")",
                "Applied Masking Strategy: " + maskingType,
                "Policy Status: " + (matchingPolicy.map(MaskingPolicy::isEnabled).orElse(true) ? "ENABLED" : "DISABLED")
        );

        List<String> trace = List.of(
                "Step 1: Target PII entity identified: " + piiType + " (Risk: " + riskLevel + ")",
                "Step 2: Current user role resolved: " + userCtx.role,
                "Step 3: Masking policy matrix evaluated: " + policyName,
                "Step 4: Decision: " + maskingType + " masking applied to response DTO"
        );

        return ExplainableDecisionDto.builder()
                .decisionId("DEC-MASK-" + piiEntityId)
                .decisionType(DecisionType.MASKING)
                .outcome(maskingType.name())
                .isHistorical(false)
                .userId(userCtx.userId)
                .username(userCtx.username)
                .role(userCtx.role.name())
                .piiEntityId(piiEntityId)
                .piiType(piiType.name())
                .riskLevel(riskLevel.name())
                .maskingType(maskingType.name())
                .policyId(matchingPolicy.map(MaskingPolicy::getId).orElse(null))
                .policyName(policyName)
                .reason(reason)
                .factors(factors)
                .decisionTrace(trace)
                .timestamp(LocalDateTime.now())
                .resourceType("PII_ENTITY")
                .resourceId(String.valueOf(piiEntityId))
                .build();
    }

    private ExplainableDecisionDto explainCurrentAccess(Permission requiredPermission, UserContext userCtx) {
        if (requiredPermission == null) {
            throw new IllegalArgumentException("requiredPermission is required for ACCESS explanation");
        }

        List<RolePermission> rolePerms = rolePermissionRepository.findByRole(userCtx.role);
        boolean granted = rolePerms.stream().anyMatch(rp -> rp.getPermission() == requiredPermission);

        String outcome = granted ? "ALLOWED" : "DENIED";
        String reason = granted
                ? "Access was allowed because the authenticated user role (" + userCtx.role + ") has the required " + requiredPermission + " permission."
                : "Access was denied because the " + userCtx.role + " role does not have the required " + requiredPermission + " permission.";

        List<String> factors = List.of(
                "Authenticated User: " + userCtx.username,
                "Assigned User Role: " + userCtx.role,
                "Requested Permission: " + requiredPermission,
                "Permission Status in RBAC Matrix: " + (granted ? "GRANTED" : "NOT GRANTED")
        );

        List<String> trace = List.of(
                "Step 1: SecurityContext user resolution: " + userCtx.username + " (" + userCtx.role + ")",
                "Step 2: Intercepted API / Resource authority requirement: " + requiredPermission,
                "Step 3: Evaluated RBAC role_permissions mapping for " + userCtx.role,
                "Step 4: Final Access Decision: " + outcome
        );

        return ExplainableDecisionDto.builder()
                .decisionId("DEC-ACC-" + System.currentTimeMillis())
                .decisionType(DecisionType.ACCESS)
                .outcome(outcome)
                .isHistorical(false)
                .userId(userCtx.userId)
                .username(userCtx.username)
                .role(userCtx.role.name())
                .requiredPermission(requiredPermission.name())
                .permissionGranted(granted)
                .reason(reason)
                .factors(factors)
                .decisionTrace(trace)
                .timestamp(LocalDateTime.now())
                .resourceType("API_ENDPOINT")
                .resourceId(requiredPermission.name())
                .build();
    }

    // ── Historical Audit Log Decision Explanation ──────────────────────────────

    private ExplainableDecisionDto explainHistoricalAuditLog(Long auditLogId) {
        AuditLog logItem = auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new NoSuchElementException("Audit Log record not found with ID: " + auditLogId));

        return mapAuditLogToHistoricalDto(logItem);
    }

    private ExplainableDecisionDto mapAuditLogToHistoricalDto(AuditLog logItem) {
        boolean hasMetadata = (logItem.getMaskingType() != null || logItem.getRiskLevel() != null || logItem.getPermission() != null);

        String outcome;
        if (!hasMetadata) {
            outcome = "HISTORICAL_EXPLANATION_UNAVAILABLE";
        } else if (logItem.getMaskingType() != null) {
            outcome = logItem.getMaskingType();
        } else if (logItem.getResult() == AuditResult.DENIED) {
            outcome = "DENIED";
        } else if (logItem.getResult() == AuditResult.SUCCESS) {
            outcome = "ALLOWED";
        } else {
            outcome = logItem.getResult().name();
        }

        String reason;
        if (!hasMetadata) {
            reason = "Historical audit metadata is insufficient to reconstruct the policy decision at the time of event.";
        } else if (logItem.getReason() != null) {
            reason = logItem.getReason();
        } else if (logItem.getDescription() != null) {
            reason = logItem.getDescription();
        } else {
            reason = "Recorded event: " + logItem.getAction() + " with result " + logItem.getResult();
        }

        DecisionType dType = DecisionType.ACCESS;
        if (logItem.getMaskingType() != null) dType = DecisionType.MASKING;
        else if (logItem.getRiskLevel() != null) dType = DecisionType.RISK_CLASSIFICATION;

        List<String> factors = List.of(
                "Event Action: " + logItem.getAction(),
                "Result: " + logItem.getResult(),
                "User: " + logItem.getUsername() + " (" + logItem.getRole() + ")",
                "Captured Timestamp: " + logItem.getTimestamp()
        );

        List<String> trace = List.of(
                "Step 1: Historical Audit Event retrieved [ID: " + logItem.getId() + "]",
                "Step 2: Event Correlation ID: " + (logItem.getRequestId() != null ? logItem.getRequestId() : "N/A"),
                "Step 3: Captured Historical Action: " + logItem.getAction() + " | Result: " + logItem.getResult(),
                "Step 4: Explanation Derived from Captured Metadata: " + outcome
        );

        return ExplainableDecisionDto.builder()
                .decisionId("HIST-" + logItem.getId())
                .decisionType(dType)
                .outcome(outcome)
                .isHistorical(true)
                .userId(logItem.getUserId())
                .username(logItem.getUsername())
                .role(logItem.getRole())
                .piiType(logItem.getPiiType())
                .riskLevel(logItem.getRiskLevel())
                .maskingType(logItem.getMaskingType())
                .requiredPermission(logItem.getPermission())
                .permissionGranted(logItem.getResult() == AuditResult.SUCCESS)
                .reason(reason)
                .factors(factors)
                .decisionTrace(trace)
                .timestamp(logItem.getTimestamp())
                .correlationId(logItem.getRequestId())
                .resourceType(logItem.getResourceType())
                .resourceId(logItem.getResourceId())
                .build();
    }

    // ── Helper Methods ─────────────────────────────────────────────────────────

    private static class UserContext {
        Long userId;
        String username;
        Role role;

        UserContext(Long userId, String username, Role role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
    }

    private UserContext resolveUserContext(String targetUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated access");
        }

        String currentUsername = auth.getName();
        Role currentRole = resolveRoleFromAuth(auth);

        if (targetUsername != null && !targetUsername.equalsIgnoreCase(currentUsername)) {
            // Requester attempts to explain another user's decision -> Enforce Administrative Permission Check!
            boolean isAdmin = (currentRole == Role.ROLE_SUPER_ADMIN || currentRole == Role.ROLE_SECURITY_OFFICER)
                              && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("DECISION_EXPLAIN"));
            if (!isAdmin) {
                throw new AccessDeniedException("You are not authorized to explain decisions for user: " + targetUsername);
            }

            // Target user context
            User targetUser = userRepository.findByUsername(targetUsername)
                    .orElseThrow(() -> new NoSuchElementException("Target user not found: " + targetUsername));
            return new UserContext(targetUser.getId(), targetUser.getUsername(), targetUser.getRole());
        }

        // Current authenticated user context
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        Long userId = currentUser != null ? currentUser.getId() : 1L;
        return new UserContext(userId, currentUsername, currentRole);
    }

    private Role resolveRoleFromAuth(Authentication auth) {
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String a = authority.getAuthority();
            if (a.equals("ROLE_SUPER_ADMIN")) return Role.ROLE_SUPER_ADMIN;
            if (a.equals("ROLE_SECURITY_OFFICER")) return Role.ROLE_SECURITY_OFFICER;
            if (a.equals("ROLE_AUDITOR")) return Role.ROLE_AUDITOR;
        }
        return Role.ROLE_VIEWER;
    }

    private List<String> generateRiskFactors(PiiEntityType piiType, RiskLevel riskLevel) {
        if (riskLevel == RiskLevel.CRITICAL) {
            return List.of(
                    "Highly sensitive national identity or financial identifier (" + piiType + ")",
                    "High impact and severe legal compliance risk if compromised",
                    "Requires mandatory dynamic masking and strict role-based access control"
            );
        } else if (riskLevel == RiskLevel.HIGH) {
            return List.of(
                    "Sensitive personal data element (" + piiType + ")",
                    "Significant privacy and security exposure risk",
                    "Requires role-based full or partial masking policies"
            );
        } else if (riskLevel == RiskLevel.MEDIUM) {
            return List.of(
                    "Personally identifiable contact metadata (" + piiType + ")",
                    "Moderate privacy exposure impact",
                    "Subject to standard organizational masking policies"
            );
        } else {
            return List.of(
                    "Low sensitivity public or internal data attribute (" + piiType + ")",
                    "Minimal privacy exposure impact"
            );
        }
    }
}
