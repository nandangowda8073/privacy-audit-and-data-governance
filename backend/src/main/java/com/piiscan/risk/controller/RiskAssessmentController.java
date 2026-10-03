package com.piiscan.risk.controller;

import com.piiscan.platform.common.ApiResponse;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.risk.dto.RiskAssessmentDto;
import com.piiscan.risk.dto.RiskSummaryDto;
import com.piiscan.risk.entity.RiskAssessment;
import com.piiscan.risk.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/risk-assessments")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentRepository riskAssessmentRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ResponseEntity<ApiResponse<List<RiskAssessmentDto>>> getAllRiskAssessments() {
        List<RiskAssessmentDto> dtos = riskAssessmentRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved all risk assessments", dtos));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ResponseEntity<ApiResponse<RiskAssessmentDto>> getRiskAssessmentById(@PathVariable Long id) {
        return riskAssessmentRepository.findById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.success("Successfully retrieved risk assessment", toDto(r))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Risk assessment not found")));
    }

    @GetMapping("/risk/{riskLevel}")
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ResponseEntity<ApiResponse<List<RiskAssessmentDto>>> getByRiskLevel(@PathVariable RiskLevel riskLevel) {
        List<RiskAssessmentDto> dtos = riskAssessmentRepository.findByRiskLevel(riskLevel)
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved risk assessments for level " + riskLevel, dtos));
    }

    @GetMapping("/pii/{piiType}")
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ResponseEntity<ApiResponse<List<RiskAssessmentDto>>> getByPiiType(@PathVariable PiiEntityType piiType) {
        List<RiskAssessmentDto> dtos = riskAssessmentRepository.findByPiiType(piiType)
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved risk assessments for PII type " + piiType, dtos));
    }

    @GetMapping("/scan/{scanId}")
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ResponseEntity<ApiResponse<List<RiskAssessmentDto>>> getByScanId(@PathVariable String scanId) {
        // Using source as scanId for now
        List<RiskAssessmentDto> dtos = riskAssessmentRepository.findBySource(scanId)
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved risk assessments for scan " + scanId, dtos));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ResponseEntity<ApiResponse<RiskSummaryDto>> getRiskSummary() {
        RiskSummaryDto summary = RiskSummaryDto.builder()
                .low(riskAssessmentRepository.countByRiskLevel(RiskLevel.LOW))
                .medium(riskAssessmentRepository.countByRiskLevel(RiskLevel.MEDIUM))
                .high(riskAssessmentRepository.countByRiskLevel(RiskLevel.HIGH))
                .critical(riskAssessmentRepository.countByRiskLevel(RiskLevel.CRITICAL))
                .build();
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved risk summary", summary));
    }

    private RiskAssessmentDto toDto(RiskAssessment entity) {
        return RiskAssessmentDto.builder()
                .id(entity.getId())
                .piiEntityId(entity.getPiiEntity() != null ? entity.getPiiEntity().getId() : null)
                .piiType(entity.getPiiType())
                .riskLevel(entity.getRiskLevel())
                .riskScore(entity.getRiskScore())
                .reason(entity.getReason())
                .source(entity.getPiiEntity() != null ? entity.getPiiEntity().getSource() : null)
                .columnName(entity.getPiiEntity() != null ? entity.getPiiEntity().getColumnName() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
