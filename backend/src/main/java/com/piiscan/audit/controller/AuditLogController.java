package com.piiscan.audit.controller;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import com.piiscan.audit.service.AuditLogService;
import com.piiscan.platform.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) AuditResult result,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long databaseId,
            @RequestParam(required = false) String scanId,
            @RequestParam(required = false) String piiType,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> auditLogs = auditLogService.getAuditLogs(
                action, result, username, role, resourceType, databaseId, scanId,
                piiType, riskLevel, search, startDate, endDate, pageable
        );

        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", auditLogs));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getRecentAuditLogs() {
        List<AuditLog> recent = auditLogService.getRecentAuditLogs();
        return ResponseEntity.ok(ApiResponse.success("Recent audit logs retrieved successfully", recent));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ResponseEntity<ApiResponse<AuditLog>> getAuditLogById(@PathVariable Long id) {
        AuditLog auditLog = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success("Audit log retrieved successfully", auditLog));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('AUDIT_LOG_EXPORT')")
    public ResponseEntity<byte[]> exportAuditLogsCsv(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) AuditResult result,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        byte[] csvData = auditLogService.exportAuditLogsCsv(
                action, result, username, role, resourceType, search, startDate, endDate
        );

        auditLogService.logReportEvent(
                AuditAction.AUDIT_LOG_EXPORTED,
                AuditResult.SUCCESS,
                "CSV",
                "AUDIT_LOG_EXPORT",
                "Exported audit logs to CSV"
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit_logs.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
