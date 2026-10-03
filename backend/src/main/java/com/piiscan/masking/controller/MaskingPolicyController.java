package com.piiscan.masking.controller;

import com.piiscan.auth.entity.Role;
import com.piiscan.masking.dto.*;
import com.piiscan.masking.entity.MaskingPolicy;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.masking.repository.MaskingPolicyRepository;
import com.piiscan.masking.service.DataMaskingService;
import com.piiscan.masking.service.MaskingAuditLogger;
import com.piiscan.masking.service.MaskingPolicyService;
import com.piiscan.platform.common.ApiResponse;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.risk.service.RiskClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/masking")
@RequiredArgsConstructor
public class MaskingPolicyController {

    private final MaskingPolicyRepository  policyRepository;
    private final MaskingPolicyService     policyService;
    private final DataMaskingService       dataMaskingService;
    private final RiskClassificationService riskClassificationService;
    private final MaskingAuditLogger       auditLogger;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping("/policies")
    @PreAuthorize("hasAuthority('MASKING_POLICY_VIEW')")
    public ResponseEntity<ApiResponse<List<MaskingPolicyDto>>> getAllPolicies() {
        List<MaskingPolicyDto> dtos = policyRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Policies retrieved", dtos));
    }

    @GetMapping("/policies/{id}")
    @PreAuthorize("hasAuthority('MASKING_POLICY_VIEW')")
    public ResponseEntity<ApiResponse<MaskingPolicyDto>> getPolicyById(@PathVariable Long id) {
        return policyRepository.findById(id)
                .map(p -> ResponseEntity.ok(ApiResponse.success("Policy retrieved", toDto(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Policy not found")));
    }

    @PostMapping("/policies")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECURITY_OFFICER')")
    public ResponseEntity<ApiResponse<MaskingPolicyDto>> createPolicy(@RequestBody MaskingPolicyRequest request) {
        MaskingPolicy policy = MaskingPolicy.builder()
                .role(request.getRole())
                .piiType(request.getPiiType())
                .riskLevel(request.getRiskLevel())
                .maskingType(request.getMaskingType())
                .description(request.getDescription())
                .enabled(request.isEnabled())
                .build();
        MaskingPolicy saved = policyRepository.save(policy);
        auditLogger.logPolicyChange(currentUsername(), "POLICY_CREATED", saved.getId());
        return ResponseEntity.ok(ApiResponse.success("Policy created", toDto(saved)));
    }

    @PutMapping("/policies/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECURITY_OFFICER')")
    public ResponseEntity<ApiResponse<MaskingPolicyDto>> updatePolicy(
            @PathVariable Long id, @RequestBody MaskingPolicyRequest request) {
        return policyRepository.findById(id).map(policy -> {
            policy.setRole(request.getRole());
            policy.setPiiType(request.getPiiType());
            policy.setRiskLevel(request.getRiskLevel());
            policy.setMaskingType(request.getMaskingType());
            policy.setDescription(request.getDescription());
            policy.setEnabled(request.isEnabled());
            policy.setUpdatedAt(LocalDateTime.now());
            MaskingPolicy saved = policyRepository.save(policy);
            auditLogger.logPolicyChange(currentUsername(), "POLICY_UPDATED", saved.getId());
            return ResponseEntity.ok(ApiResponse.success("Policy updated", toDto(saved)));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("Policy not found")));
    }

    @DeleteMapping("/policies/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECURITY_OFFICER')")
    public ResponseEntity<ApiResponse<String>> deletePolicy(@PathVariable Long id) {
        if (!policyRepository.existsById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("Policy not found"));
        }
        policyRepository.deleteById(id);
        auditLogger.logPolicyChange(currentUsername(), "POLICY_DELETED", id);
        return ResponseEntity.ok(ApiResponse.success("Policy deleted", "OK"));
    }

    // ── Preview ───────────────────────────────────────────────────────────────

    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECURITY_OFFICER')")
    public ResponseEntity<ApiResponse<MaskingPreviewResponse>> previewMasking(
            @RequestBody MaskingPreviewRequest request) {

        // Determine risk level from our classification service
        RiskLevel riskLevel = riskClassificationService.determineRiskLevel(request.getPiiType());

        // Resolve role of current user
        Role role = resolveCurrentRole();
        MaskingType maskingType = policyService.resolve(role, request.getPiiType(), riskLevel);
        String reason = policyService.generateReason(role, request.getPiiType(), riskLevel, maskingType);
        String masked = dataMaskingService.applyMask(request.getValue(), request.getPiiType(), maskingType);

        MaskingPreviewResponse response = MaskingPreviewResponse.builder()
                .piiType(request.getPiiType())
                .riskLevel(riskLevel)
                .maskingType(maskingType)
                .maskedValue(masked)
                .reason(reason)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Masking preview generated", response));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MaskingPolicyDto toDto(MaskingPolicy p) {
        return MaskingPolicyDto.builder()
                .id(p.getId())
                .role(p.getRole())
                .piiType(p.getPiiType())
                .riskLevel(p.getRiskLevel())
                .maskingType(p.getMaskingType())
                .description(p.getDescription())
                .enabled(p.isEnabled())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "UNKNOWN";
    }

    private Role resolveCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Role.ROLE_VIEWER;
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> {
                    try { return Role.valueOf(a); } catch (Exception e) { return null; }
                })
                .filter(r -> r != null)
                .findFirst()
                .orElse(Role.ROLE_VIEWER);
    }
}
