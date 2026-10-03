package com.piiscan.audit.entity;

import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_user_id", columnList = "userId"),
    @Index(name = "idx_audit_username", columnList = "username"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_result", columnList = "result"),
    @Index(name = "idx_audit_role", columnList = "role"),
    @Index(name = "idx_audit_resource_type", columnList = "resourceType"),
    @Index(name = "idx_audit_scan_id", columnList = "scanId"),
    @Index(name = "idx_audit_database_id", columnList = "databaseId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String username;

    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    private String resourceType;
    private String resourceId;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditResult result;

    @Column(length = 1000)
    private String reason;

    private String permission;
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Domain Specific Metadata (NO RAW PII VALUES EVER)
    private Long databaseId;
    private String databaseName;
    private String scanId;
    private String tableName;
    private String piiType;
    private String riskLevel;
    private String maskingType;
    private String requestId;
    private String endpoint;
    private String httpMethod;

    @PrePersist
    public void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
