package com.piiscan.masking.service;

import com.piiscan.auth.entity.Role;
import com.piiscan.masking.entity.MaskingPolicy;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.masking.repository.MaskingPolicyRepository;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Resolves the effective masking type for a given role + PII type + risk level.
 * Falls back to secure defaults if no DB policy matches.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaskingPolicyService {

    private final MaskingPolicyRepository policyRepository;

    /**
     * Determine the masking type. Priority: DB policy → secure default.
     */
    public MaskingType resolve(Role role, PiiEntityType piiType, RiskLevel riskLevel) {
        Optional<MaskingPolicy> policy = policyRepository
                .findByRoleAndPiiTypeAndRiskLevelAndEnabledTrue(role, piiType, riskLevel);

        if (policy.isPresent()) {
            log.debug("Applying DB policy for role={}, piiType={}, riskLevel={} → {}",
                    role, piiType, riskLevel, policy.get().getMaskingType());
            return policy.get().getMaskingType();
        }

        // Fallback: secure defaults
        MaskingType defaultMask = getSecureDefault(role, riskLevel);
        log.debug("No policy found for role={}, piiType={}, riskLevel={}. Applying default: {}",
                role, piiType, riskLevel, defaultMask);
        return defaultMask;
    }

    /**
     * Centralized secure defaults table. Change this to adjust global behavior.
     *
     * SUPER_ADMIN / SECURITY_OFFICER:
     *   LOW/MEDIUM → NONE, HIGH/CRITICAL → PARTIAL
     * AUDITOR:
     *   LOW → NONE, MEDIUM/HIGH → PARTIAL, CRITICAL → FULL
     * VIEWER:
     *   LOW → NONE, MEDIUM → PARTIAL, HIGH/CRITICAL → FULL
     */
    public MaskingType getSecureDefault(Role role, RiskLevel riskLevel) {
        if (role == null) return MaskingType.FULL;

        return switch (role) {
            case ROLE_SUPER_ADMIN, ROLE_SECURITY_OFFICER -> switch (riskLevel) {
                case LOW, MEDIUM -> MaskingType.NONE;
                case HIGH, CRITICAL -> MaskingType.PARTIAL;
            };
            case ROLE_AUDITOR -> switch (riskLevel) {
                case LOW -> MaskingType.NONE;
                case MEDIUM, HIGH -> MaskingType.PARTIAL;
                case CRITICAL -> MaskingType.FULL;
            };
            case ROLE_VIEWER -> switch (riskLevel) {
                case LOW -> MaskingType.NONE;
                case MEDIUM -> MaskingType.PARTIAL;
                case HIGH, CRITICAL -> MaskingType.FULL;
            };
        };
    }

    /**
     * Generate a human-readable reason for the masking decision.
     */
    public String generateReason(Role role, PiiEntityType piiType, RiskLevel riskLevel, MaskingType maskingType) {
        String roleName = role != null ? role.name().replace("ROLE_", "") : "UNKNOWN";
        return switch (maskingType) {
            case FULL -> String.format(
                    "%s is classified as %s risk PII. %s role is not authorized to view the original value.",
                    piiType, riskLevel, roleName);
            case PARTIAL -> String.format(
                    "%s is %s risk PII. %s role may access the record but cannot view the complete identifier.",
                    piiType, riskLevel, roleName);
            case NONE -> String.format(
                    "%s is %s risk PII. %s role is authorized to view the original value.",
                    piiType, riskLevel, roleName);
        };
    }
}
