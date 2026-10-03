package com.piiscan.masking.service;

import com.piiscan.auth.entity.Role;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.pii.dto.PiiEntityDto;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrator that intercepts PiiEntityDtos and applies dynamic masking
 * based on the authenticated user's role and active policies.
 *
 * The original database value is NEVER modified. Only the DTO is altered.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicMaskingService {

    private final MaskingPolicyService policyService;
    private final DataMaskingService   dataMaskingService;
    private final MaskingAuditLogger   auditLogger;

    /**
     * Apply dynamic masking to a list of PiiEntityDtos.
     */
    public List<PiiEntityDto> maskAll(List<PiiEntityDto> dtos) {
        Role role = resolveCurrentUserRole();
        return dtos.stream()
                .map(dto -> maskSingle(dto, role))
                .collect(Collectors.toList());
    }

    /**
     * Apply dynamic masking to a single PiiEntityDto.
     */
    public PiiEntityDto maskSingle(PiiEntityDto dto, Role role) {
        PiiEntityType piiType  = dto.getEntityType();
        RiskLevel riskLevel    = dto.getRiskLevel();

        if (piiType == null || riskLevel == null) return dto;

        MaskingType maskingType = policyService.resolve(role, piiType, riskLevel);
        String reason           = policyService.generateReason(role, piiType, riskLevel, maskingType);

        // Audit log metadata — raw value is never logged
        auditLogger.log(resolveCurrentUsername(), role, piiType, riskLevel, maskingType, reason);

        // Apply masking to originalValue — DB record is untouched
        String maskedOriginal = dataMaskingService.applyMask(dto.getOriginalValue(), piiType, maskingType);

        // Build a new DTO with the masked value replacing the sensitive originalValue
        return PiiEntityDto.builder()
                .id(dto.getId())
                .entityType(dto.getEntityType())
                .category(dto.getCategory())
                .originalValue(maskedOriginal)               // ← masked version replaces original in DTO
                .maskedValue(dto.getMaskedValue())           // keep the already-masked field
                .confidenceScore(dto.getConfidenceScore())
                .riskLevel(dto.getRiskLevel())
                .detectionMethod(dto.getDetectionMethod())
                .source(dto.getSource())
                .columnName(dto.getColumnName())
                .startIndex(dto.getStartIndex())
                .endIndex(dto.getEndIndex())
                .detectedBy(dto.getDetectedBy())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    public Role resolveCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Role.ROLE_VIEWER;

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String a = authority.getAuthority();
            if (a.equals("ROLE_SUPER_ADMIN"))       return Role.ROLE_SUPER_ADMIN;
            if (a.equals("ROLE_SECURITY_OFFICER"))  return Role.ROLE_SECURITY_OFFICER;
            if (a.equals("ROLE_AUDITOR"))            return Role.ROLE_AUDITOR;
        }
        return Role.ROLE_VIEWER;
    }

    private String resolveCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "UNKNOWN";
    }
}
