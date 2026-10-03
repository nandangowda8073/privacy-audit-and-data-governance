package com.piiscan.audit.enums;

public enum AuditAction {
    // Auth
    AUTH_LOGIN,
    AUTH_LOGOUT,
    AUTH_LOGIN_FAILED,

    // User Management
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    USER_ENABLE,
    USER_DISABLE,
    USER_ROLE_CHANGED,

    // RBAC & Permissions
    ROLE_PERMISSION_UPDATED,
    ACCESS_GRANTED,
    ACCESS_DENIED,

    // Database Management
    DATABASE_CREATE,
    DATABASE_UPDATE,
    DATABASE_DELETE,
    DATABASE_CONNECTION_TEST,

    // Scanner & Scheduler
    SCAN_STARTED,
    SCAN_COMPLETED,
    SCAN_FAILED,
    SCAN_CANCELLED,
    SCAN_SCHEDULED,

    // PII Detection & Risk
    PII_DETECTED,
    RISK_CLASSIFIED,

    // Masking & Sensitive Data Access
    SENSITIVE_DATA_ACCESS,
    MASKING_POLICY_CREATED,
    MASKING_POLICY_UPDATED,
    MASKING_POLICY_DELETED,
    MASKING_APPLIED,

    // Reports & Export
    REPORT_GENERATED,
    REPORT_DOWNLOADED,
    AUDIT_LOG_EXPORTED,

    // Explainable Decisions
    DECISION_EXPLAINED
}
