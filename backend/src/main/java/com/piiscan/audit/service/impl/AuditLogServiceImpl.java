package com.piiscan.audit.service.impl;

import com.piiscan.audit.context.AuditRequestContext;
import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import com.piiscan.audit.repository.AuditLogRepository;
import com.piiscan.audit.service.AuditLogService;
import com.piiscan.platform.common.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog logEvent(AuditAction action, AuditResult result, String resourceType, String resourceId,
                             String description, String reason, String permission, Map<String, Object> metadata) {
        try {
            AuditRequestContext reqCtx = AuditRequestContext.get();
            String username = resolveCurrentUsername();
            String role = resolveCurrentRole();
            String requestId = (reqCtx != null && reqCtx.getRequestId() != null && !reqCtx.getRequestId().isBlank())
                    ? reqCtx.getRequestId()
                    : "SYS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .username(username)
                    .role(role)
                    .action(action)
                    .result(result)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .description(description)
                    .reason(reason)
                    .permission(permission)
                    .ipAddress(reqCtx != null ? reqCtx.getIpAddress() : null)
                    .userAgent(reqCtx != null ? reqCtx.getUserAgent() : null)
                    .requestId(requestId)
                    .endpoint(reqCtx != null ? reqCtx.getEndpoint() : null)
                    .httpMethod(reqCtx != null ? reqCtx.getHttpMethod() : null)
                    .timestamp(LocalDateTime.now());

            if (metadata != null) {
                if (metadata.containsKey("databaseId") && metadata.get("databaseId") instanceof Long dbId) {
                    builder.databaseId(dbId);
                }
                if (metadata.containsKey("databaseName") && metadata.get("databaseName") instanceof String dbName) {
                    builder.databaseName(dbName);
                }
                if (metadata.containsKey("scanId") && metadata.get("scanId") instanceof String scanId) {
                    builder.scanId(scanId);
                }
                if (metadata.containsKey("tableName") && metadata.get("tableName") instanceof String tableName) {
                    builder.tableName(tableName);
                }
                if (metadata.containsKey("piiType") && metadata.get("piiType") instanceof String piiType) {
                    builder.piiType(piiType);
                }
                if (metadata.containsKey("riskLevel") && metadata.get("riskLevel") instanceof String riskLevel) {
                    builder.riskLevel(riskLevel);
                }
                if (metadata.containsKey("maskingType") && metadata.get("maskingType") instanceof String maskingType) {
                    builder.maskingType(maskingType);
                }
                if (metadata.containsKey("userId") && metadata.get("userId") instanceof Long userId) {
                    builder.userId(userId);
                }
            }

            AuditLog saved = auditLogRepository.save(builder.build());
            log.info("AUDIT LOG PERSISTED [ID: {}] [ReqID: {}] | Action: {} | User: {} ({}) | Result: {} | Resource: {}",
                    saved.getId(), requestId, action, username, role, result, resourceType);
            return saved;
        } catch (Exception ex) {
            log.error("Failed to persist audit log entry for action: {}", action, ex);
            return null;
        }
    }

    @Override
    public AuditLog logAuthEvent(AuditAction action, AuditResult result, String username, String role, String description, String reason) {
        String resolvedUser = (username != null && !username.isBlank()) ? username : resolveCurrentUsername();
        String resolvedRole = (role != null && !role.isBlank()) ? role : resolveCurrentRole();

        AuditRequestContext reqCtx = AuditRequestContext.get();
        String requestId = (reqCtx != null && reqCtx.getRequestId() != null && !reqCtx.getRequestId().isBlank())
                ? reqCtx.getRequestId()
                : "SYS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(resolvedUser)
                    .role(resolvedRole)
                    .action(action)
                    .result(result)
                    .resourceType("AUTH")
                    .description(description)
                    .reason(reason)
                    .ipAddress(reqCtx != null ? reqCtx.getIpAddress() : null)
                    .userAgent(reqCtx != null ? reqCtx.getUserAgent() : null)
                    .requestId(requestId)
                    .endpoint(reqCtx != null ? reqCtx.getEndpoint() : null)
                    .httpMethod(reqCtx != null ? reqCtx.getHttpMethod() : null)
                    .timestamp(LocalDateTime.now())
                    .build();
            return auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.error("Failed to log auth event for user: {}", resolvedUser, ex);
            return null;
        }
    }

    @Override
    public AuditLog logSecurityEvent(AuditAction action, AuditResult result, String username, String resourceType, String resourceId, String description, String reason, String permission) {
        String user = (username != null && !username.isBlank()) ? username : resolveCurrentUsername();
        return logEvent(action, result, resourceType, resourceId, description, reason, permission, null);
    }

    @Override
    public AuditLog logDatabaseEvent(AuditAction action, AuditResult result, Long dbId, String dbName, String description, String reason) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("databaseId", dbId);
        meta.put("databaseName", dbName);
        return logEvent(action, result, "DATABASE", dbName, description, reason, null, meta);
    }

    @Override
    public AuditLog logScanEvent(AuditAction action, AuditResult result, String scanId, Long dbId, String dbName, String tableName, String description, String reason) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("scanId", scanId);
        meta.put("databaseId", dbId);
        meta.put("databaseName", dbName);
        meta.put("tableName", tableName);
        return logEvent(action, result, "SCAN", scanId, description, reason, null, meta);
    }

    @Override
    public AuditLog logPiiEvent(AuditAction action, AuditResult result, String scanId, String dbName, String tableName, String piiType, String riskLevel, Integer count) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("scanId", scanId);
        meta.put("databaseName", dbName);
        meta.put("tableName", tableName);
        meta.put("piiType", piiType);
        meta.put("riskLevel", riskLevel);
        String desc = String.format("PII entity detected: %s (Risk: %s, Findings: %d)", piiType, riskLevel, count != null ? count : 1);
        return logEvent(action, result, "PII_DETECTION", piiType, desc, "Automated PII detection rule match", null, meta);
    }

    @Override
    public AuditLog logRiskEvent(AuditAction action, AuditResult result, String scanId, String piiType, String riskLevel, String reason) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("scanId", scanId);
        meta.put("piiType", piiType);
        meta.put("riskLevel", riskLevel);
        String desc = String.format("Assigned risk classification %s to PII type %s", riskLevel, piiType);
        return logEvent(action, result, "RISK_ASSESSMENT", piiType, desc, reason, null, meta);
    }

    @Override
    public AuditLog logMaskingEvent(AuditAction action, AuditResult result, String username, String role, String piiType, String riskLevel, String maskingType, String reason) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("piiType", piiType);
        meta.put("riskLevel", riskLevel);
        meta.put("maskingType", maskingType);
        String desc = String.format("Applied %s masking on PII %s (Risk: %s)", maskingType, piiType, riskLevel);
        return logEvent(action, result, "DATA_MASKING", piiType, desc, reason, null, meta);
    }

    @Override
    public AuditLog logUserManagementEvent(AuditAction action, AuditResult result, String targetUsername, String oldRoleOrStatus, String newRoleOrStatus, String reason) {
        String desc = String.format("User management operation on '%s': %s -> %s", targetUsername, oldRoleOrStatus, newRoleOrStatus);
        return logEvent(action, result, "USER_MANAGEMENT", targetUsername, desc, reason, null, null);
    }

    @Override
    public AuditLog logPermissionEvent(AuditAction action, AuditResult result, String role, String permission, String description) {
        return logEvent(action, result, "ROLE_PERMISSION", role, description, "Role permission matrix updated", permission, null);
    }

    @Override
    public AuditLog logReportEvent(AuditAction action, AuditResult result, String reportType, String reportId, String description) {
        return logEvent(action, result, "REPORT", reportId, description, "Report operation executed", null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(AuditAction action, AuditResult result, String username, String role,
                                        String resourceType, Long databaseId, String scanId, String piiType,
                                        String riskLevel, String search, String startDate, String endDate,
                                        Pageable pageable) {

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (result != null) {
                predicates.add(cb.equal(root.get("result"), result));
            }
            if (username != null && !username.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            }
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (resourceType != null && !resourceType.isBlank()) {
                predicates.add(cb.equal(root.get("resourceType"), resourceType));
            }
            if (databaseId != null) {
                predicates.add(cb.equal(root.get("databaseId"), databaseId));
            }
            if (scanId != null && !scanId.isBlank()) {
                predicates.add(cb.equal(root.get("scanId"), scanId));
            }
            if (piiType != null && !piiType.isBlank()) {
                predicates.add(cb.equal(root.get("piiType"), piiType));
            }
            if (riskLevel != null && !riskLevel.isBlank()) {
                predicates.add(cb.equal(root.get("riskLevel"), riskLevel));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate searchPred = cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("reason")), pattern),
                        cb.like(cb.lower(root.get("resourceType")), pattern),
                        cb.like(cb.lower(root.get("ipAddress")), pattern),
                        cb.like(cb.lower(root.get("requestId")), pattern)
                );
                predicates.add(searchPred);
            }
            if (startDate != null && !startDate.isBlank()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(startDate);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), start));
                } catch (Exception ignored) {}
            }
            if (endDate != null && !endDate.isBlank()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(endDate);
                    predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), end));
                } catch (Exception ignored) {}
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLog getAuditLogById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log record not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAuditLogsCsv(AuditAction action, AuditResult result, String username, String role,
                                      String resourceType, String search, String startDate, String endDate) {
        Page<AuditLog> page = getAuditLogs(action, result, username, role, resourceType, null, null, null, null, search, startDate, endDate, Pageable.unpaged());
        List<AuditLog> logs = page.getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // CSV Header
        writer.println("ID,Timestamp,Request ID,User,Role,Action,Result,Resource Type,Resource ID,Description,Reason,IP Address,User Agent,Permission");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AuditLog logItem : logs) {
            writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    logItem.getId(),
                    logItem.getTimestamp() != null ? logItem.getTimestamp().format(fmt) : "",
                    escapeCsv(logItem.getRequestId()),
                    escapeCsv(logItem.getUsername()),
                    escapeCsv(logItem.getRole()),
                    logItem.getAction(),
                    logItem.getResult(),
                    escapeCsv(logItem.getResourceType()),
                    escapeCsv(logItem.getResourceId()),
                    escapeCsv(logItem.getDescription()),
                    escapeCsv(logItem.getReason()),
                    escapeCsv(logItem.getIpAddress()),
                    escapeCsv(logItem.getUserAgent()),
                    escapeCsv(logItem.getPermission())
            );
        }

        writer.flush();
        return out.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String resolveCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private String resolveCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            return auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(a -> a.startsWith("ROLE_"))
                    .findFirst()
                    .orElse("ROLE_USER");
        }
        return "SYSTEM";
    }
}
