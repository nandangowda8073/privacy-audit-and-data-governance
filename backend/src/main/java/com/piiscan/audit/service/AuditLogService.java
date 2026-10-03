package com.piiscan.audit.service;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AuditLogService {

    AuditLog logEvent(AuditAction action, AuditResult result, String resourceType, String resourceId,
                     String description, String reason, String permission, Map<String, Object> metadata);

    AuditLog logAuthEvent(AuditAction action, AuditResult result, String username, String role, String description, String reason);

    AuditLog logSecurityEvent(AuditAction action, AuditResult result, String username, String resourceType, String resourceId, String description, String reason, String permission);

    AuditLog logDatabaseEvent(AuditAction action, AuditResult result, Long dbId, String dbName, String description, String reason);

    AuditLog logScanEvent(AuditAction action, AuditResult result, String scanId, Long dbId, String dbName, String tableName, String description, String reason);

    AuditLog logPiiEvent(AuditAction action, AuditResult result, String scanId, String dbName, String tableName, String piiType, String riskLevel, Integer count);

    AuditLog logRiskEvent(AuditAction action, AuditResult result, String scanId, String piiType, String riskLevel, String reason);

    AuditLog logMaskingEvent(AuditAction action, AuditResult result, String username, String role, String piiType, String riskLevel, String maskingType, String reason);

    AuditLog logUserManagementEvent(AuditAction action, AuditResult result, String targetUsername, String oldRoleOrStatus, String newRoleOrStatus, String reason);

    AuditLog logPermissionEvent(AuditAction action, AuditResult result, String role, String permission, String description);

    AuditLog logReportEvent(AuditAction action, AuditResult result, String reportType, String reportId, String description);

    Page<AuditLog> getAuditLogs(AuditAction action, AuditResult result, String username, String role,
                                String resourceType, Long databaseId, String scanId, String piiType,
                                String riskLevel, String search, String startDate, String endDate,
                                Pageable pageable);

    AuditLog getAuditLogById(Long id);

    List<AuditLog> getRecentAuditLogs();

    byte[] exportAuditLogsCsv(AuditAction action, AuditResult result, String username, String role,
                              String resourceType, String search, String startDate, String endDate);
}
