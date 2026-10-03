package com.piiscan.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecurityAuditLogger {

    @org.springframework.beans.factory.annotation.Autowired
    private com.piiscan.audit.service.AuditLogService auditLogService;

    public void logAccessDenied(String username, String resource) {
        log.warn("AUDIT [ACCESS_DENIED] User: '{}' attempted to access resource: '{}'", username, resource);
        if (auditLogService != null) {
            auditLogService.logSecurityEvent(
                com.piiscan.audit.enums.AuditAction.ACCESS_DENIED,
                com.piiscan.audit.enums.AuditResult.DENIED,
                username,
                "API_ENDPOINT",
                resource,
                "Access denied to resource: " + resource,
                "Insufficient authority or missing permission",
                null
            );
        }
    }

    public void logRoleChange(String performedBy, String targetUser, String newRole) {
        log.info("AUDIT [ROLE_CHANGED] Admin: '{}' changed role of user: '{}' to '{}'", performedBy, targetUser, newRole);
        if (auditLogService != null) {
            auditLogService.logUserManagementEvent(
                com.piiscan.audit.enums.AuditAction.USER_ROLE_CHANGED,
                com.piiscan.audit.enums.AuditResult.SUCCESS,
                targetUser,
                "PREVIOUS_ROLE",
                newRole,
                "Admin changed user role to " + newRole
            );
        }
    }

    public void logPermissionChange(String performedBy, String role) {
        log.info("AUDIT [PERMISSION_CHANGED] Admin: '{}' updated permissions for role: '{}'", performedBy, role);
        if (auditLogService != null) {
            auditLogService.logPermissionEvent(
                com.piiscan.audit.enums.AuditAction.ROLE_PERMISSION_UPDATED,
                com.piiscan.audit.enums.AuditResult.SUCCESS,
                role,
                null,
                "Role permissions matrix updated for role: " + role
            );
        }
    }

    public void logUserStatusChange(String performedBy, String targetUser, boolean active) {
        log.info("AUDIT [USER_STATUS_CHANGED] Admin: '{}' set active status of user: '{}' to '{}'", performedBy, targetUser, active);
        if (auditLogService != null) {
            auditLogService.logUserManagementEvent(
                active ? com.piiscan.audit.enums.AuditAction.USER_ENABLE : com.piiscan.audit.enums.AuditAction.USER_DISABLE,
                com.piiscan.audit.enums.AuditResult.SUCCESS,
                targetUser,
                String.valueOf(!active),
                String.valueOf(active),
                "User active status toggled to " + active
            );
        }
    }
}
