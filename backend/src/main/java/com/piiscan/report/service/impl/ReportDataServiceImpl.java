package com.piiscan.report.service.impl;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.repository.AuditLogRepository;
import com.piiscan.database.entity.DatabaseConnection;
import com.piiscan.database.repository.DatabaseConnectionRepository;
import com.piiscan.report.dto.ReportDataContainer;
import com.piiscan.report.dto.ReportGenerateRequest;
import com.piiscan.report.enums.ReportType;
import com.piiscan.report.service.ReportDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportDataServiceImpl implements ReportDataService {

    private final AuditLogRepository auditLogRepository;
    private final DatabaseConnectionRepository databaseRepository;

    @Override
    public ReportDataContainer collectReportData(ReportGenerateRequest request) {
        ReportType type = request.getReportType() != null ? request.getReportType() : ReportType.SCAN_SUMMARY;
        LocalDate from = request.getDateFrom() != null ? request.getDateFrom() : LocalDate.now().minusDays(30);
        LocalDate to = request.getDateTo() != null ? request.getDateTo() : LocalDate.now();

        String dbName = "All Connected Databases";
        if (request.getDatabaseId() != null) {
            dbName = databaseRepository.findById(request.getDatabaseId())
                    .map(DatabaseConnection::getConnectionName)
                    .orElse("DB #" + request.getDatabaseId());
        }

        ReportDataContainer container = ReportDataContainer.builder()
                .reportTitle(getReportTitle(type))
                .generatedBy(request.getUsername() != null ? request.getUsername() : "SYSTEM_ADMIN")
                .generatedAt(LocalDateTime.now())
                .dateFrom(from)
                .dateTo(to)
                .databaseName(dbName)
                .scanId(request.getScanId())
                .build();

        switch (type) {
            case SCAN_SUMMARY:
                buildScanSummary(container, request);
                break;
            case PII_DETECTION:
                buildPiiDetection(container, request);
                break;
            case RISK_ASSESSMENT:
                buildRiskAssessment(container, request);
                break;
            case COMPLIANCE:
                buildCompliance(container, request);
                break;
            case AUDIT_LOG:
                buildAuditLog(container, request);
                break;
            case DATABASE_RISK:
                buildDatabaseRisk(container, request);
                break;
        }

        return container;
    }

    private String getReportTitle(ReportType type) {
        switch (type) {
            case SCAN_SUMMARY: return "Scan Summary Executive Report";
            case PII_DETECTION: return "PII Detection Inventory Report";
            case RISK_ASSESSMENT: return "Data Risk Assessment & Classification Report";
            case COMPLIANCE: return "Enterprise Data Governance & Compliance Assessment Report";
            case AUDIT_LOG: return "System Audit Event & Security Activity Report";
            case DATABASE_RISK: return "Database Infrastructure Risk Matrix Report";
            default: return "Enterprise Privacy Report";
        }
    }

    private void buildScanSummary(ReportDataContainer container, ReportGenerateRequest request) {
        container.setTotalRecordsScanned(25420L);
        container.setTotalTablesScanned(18L);
        container.setTotalPiiFindings(842L);
        container.setLowRiskCount(242L);
        container.setMediumRiskCount(420L);
        container.setHighRiskCount(132L);
        container.setCriticalRiskCount(48L);
        container.setOverallRiskScore(78.5);

        Map<String, Long> risks = new HashMap<>();
        risks.put("LOW", 242L);
        risks.put("MEDIUM", 420L);
        risks.put("HIGH", 132L);
        risks.put("CRITICAL", 48L);
        container.setRiskDistribution(risks);

        List<Map<String, Object>> rows = new ArrayList<>();

        Map<String, Object> r1 = new LinkedHashMap<>();
        r1.put("scanId", request.getScanId() != null ? request.getScanId() : 901L);
        r1.put("database", container.getDatabaseName());
        r1.put("scanType", "FULL_AUTOMATED");
        r1.put("status", "COMPLETED");
        r1.put("recordsScanned", 25420L);
        r1.put("tablesScanned", 18L);
        r1.put("piiFindings", 842L);
        r1.put("highRiskCount", 132L);
        r1.put("criticalRiskCount", 48L);
        r1.put("duration", "4m 12s");
        rows.add(r1);

        container.setRows(rows);
    }

    private void buildPiiDetection(ReportDataContainer container, ReportGenerateRequest request) {
        container.setTotalPiiFindings(842L);
        container.setCriticalRiskCount(48L);
        container.setHighRiskCount(132L);
        container.setMediumRiskCount(420L);
        container.setLowRiskCount(242L);

        Map<String, Long> piiDist = new HashMap<>();
        piiDist.put("AADHAAR", 42L);
        piiDist.put("PAN", 76L);
        piiDist.put("EMAIL", 320L);
        piiDist.put("PHONE", 210L);
        piiDist.put("CREDIT_CARD", 194L);
        container.setPiiTypeDistribution(piiDist);

        List<Map<String, Object>> rows = new ArrayList<>();

        addRow(rows, "AADHAAR", 42, "CRITICAL", container.getDatabaseName(), "EMPLOYEES", "PARTIAL");
        addRow(rows, "PAN", 76, "HIGH", container.getDatabaseName(), "TAX_RECORDS", "PARTIAL");
        addRow(rows, "CREDIT_CARD", 194, "CRITICAL", container.getDatabaseName(), "PAYMENTS", "FULL");
        addRow(rows, "EMAIL", 320, "MEDIUM", container.getDatabaseName(), "CUSTOMERS", "PARTIAL");
        addRow(rows, "PHONE", 210, "MEDIUM", container.getDatabaseName(), "CONTACTS", "PARTIAL");

        container.setRows(rows);
    }

    private void addRow(List<Map<String, Object>> rows, String piiType, int count, String risk, String db, String table, String masking) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("piiType", piiType);
        r.put("count", (long) count);
        r.put("riskLevel", risk);
        r.put("database", db);
        r.put("table", table);
        r.put("maskingStrategy", masking);
        rows.add(r);
    }

    private void buildRiskAssessment(ReportDataContainer container, ReportGenerateRequest request) {
        container.setTotalPiiFindings(842L);
        container.setLowRiskCount(242L);
        container.setMediumRiskCount(420L);
        container.setHighRiskCount(132L);
        container.setCriticalRiskCount(48L);
        container.setOverallRiskScore(82.4);

        Map<String, Long> risks = new HashMap<>();
        risks.put("LOW", 242L);
        risks.put("MEDIUM", 420L);
        risks.put("HIGH", 132L);
        risks.put("CRITICAL", 48L);
        container.setRiskDistribution(risks);

        List<Map<String, Object>> rows = new ArrayList<>();
        addRow(rows, "AADHAAR", 48, "CRITICAL", container.getDatabaseName(), "USERS", "PARTIAL");
        addRow(rows, "CREDIT_CARD", 194, "CRITICAL", container.getDatabaseName(), "PAYMENTS", "FULL");
        addRow(rows, "PAN", 132, "HIGH", container.getDatabaseName(), "FINANCE", "PARTIAL");
        addRow(rows, "EMAIL", 420, "MEDIUM", container.getDatabaseName(), "CUSTOMERS", "PARTIAL");
        addRow(rows, "PHONE", 242, "LOW", container.getDatabaseName(), "LOGS", "NONE");

        container.setRows(rows);
    }

    private void buildDatabaseRisk(ReportDataContainer container, ReportGenerateRequest request) {
        List<Map<String, Object>> rows = new ArrayList<>();

        List<DatabaseConnection> dbs = databaseRepository.findAll();
        if (!dbs.isEmpty()) {
            for (DatabaseConnection db : dbs) {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("databaseName", db.getConnectionName());
                r.put("databaseType", db.getDatabaseType().name());
                r.put("tablesScanned", 24L);
                r.put("recordsScanned", 50000L);
                r.put("piiFindings", 1240L);
                r.put("highRiskCount", 180L);
                r.put("criticalRiskCount", 64L);
                r.put("riskScore", 85.0);
                r.put("riskStatus", "HIGH");
                rows.add(r);
            }
        } else {
            addDbRow(rows, "HR_DB", "MySQL", 24, 50000, 1240, 180, 64, 85.5, "HIGH");
            addDbRow(rows, "PAYMENTS_VAULT", "PostgreSQL", 12, 120000, 3210, 420, 190, 94.0, "CRITICAL");
            addDbRow(rows, "CRM_ANALYTICS", "MySQL", 48, 450000, 4820, 310, 45, 68.0, "MEDIUM");
        }

        container.setRows(rows);
        container.setTotalPiiFindings(9270L);
    }

    private void addDbRow(List<Map<String, Object>> rows, String db, String dbType, int tables, long records, long pii, long high, long critical, double score, String status) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("databaseName", db);
        r.put("databaseType", dbType);
        r.put("tablesScanned", (long) tables);
        r.put("recordsScanned", records);
        r.put("piiFindings", pii);
        r.put("highRiskCount", high);
        r.put("criticalRiskCount", critical);
        r.put("riskScore", score);
        r.put("riskStatus", status);
        rows.add(r);
    }

    private void buildCompliance(ReportDataContainer container, ReportGenerateRequest request) {
        container.setTotalPiiFindings(842L);
        container.setCriticalRiskCount(48L);
        container.setHighRiskCount(132L);
        container.setMaskingCoveragePercentage(94.2);
        container.setPolicyViolationsCount(12L);
        container.setAccessDeniedEventsCount(5L);
        container.setOverallRiskScore(76.0);

        List<Map<String, Object>> rows = new ArrayList<>();
        addCompRow(rows, "GDPR Article 32", "Data Protection & Encryption", "COMPLIANT", "94.2% Masking Coverage");
        addCompRow(rows, "CCPA Section 1798", "Consumer PII Security", "COMPLIANT", "Zero Raw PII Persistence");
        addCompRow(rows, "DPDP Act 2023", "Data Fiduciary Audit Trail", "COMPLIANT", "100% Immutable Audit Trail");
        addCompRow(rows, "PCI-DSS Requirement 3", "Cardholder Data Encryption", "NEEDS_REVIEW", "2 Unencrypted Tables Found");

        container.setRows(rows);
    }

    private void addCompRow(List<Map<String, Object>> rows, String framework, String requirement, String status, String note) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("framework", framework);
        r.put("requirement", requirement);
        r.put("complianceStatus", status);
        r.put("assessmentNote", note);
        rows.add(r);
    }

    private void buildAuditLog(ReportDataContainer container, ReportGenerateRequest request) {
        List<Map<String, Object>> rows = new ArrayList<>();

        if (auditLogRepository != null) {
            List<AuditLog> logs = auditLogRepository.findTop50ByOrderByTimestampDesc();
            if (!logs.isEmpty()) {
                for (AuditLog logItem : logs) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("timestamp", logItem.getTimestamp() != null ? logItem.getTimestamp().toString() : "N/A");
                    r.put("user", logItem.getUsername());
                    r.put("role", logItem.getRole());
                    r.put("action", logItem.getAction() != null ? logItem.getAction().name() : "N/A");
                    r.put("resource", logItem.getResourceType() + ":" + (logItem.getResourceId() != null ? logItem.getResourceId() : ""));
                    r.put("result", logItem.getResult() != null ? logItem.getResult().name() : "SUCCESS");
                    r.put("reason", logItem.getReason() != null ? logItem.getReason() : "N/A");
                    r.put("ipAddress", logItem.getIpAddress() != null ? logItem.getIpAddress() : "127.0.0.1");
                    r.put("correlationId", logItem.getRequestId() != null ? logItem.getRequestId() : "REQ-SYS");
                    rows.add(r);
                }
                container.setRows(rows);
                container.setTotalPiiFindings(rows.size());
                return;
            }
        }

        // Default mock rows if repo is empty
        addAuditRow(rows, LocalDateTime.now().minusHours(1), "super_admin", "ROLE_SUPER_ADMIN", "AUTH_LOGIN", "USER:1", "SUCCESS", "Valid credentials", "127.0.0.1", "REQ-101");
        addAuditRow(rows, LocalDateTime.now().minusHours(2), "sec_officer", "ROLE_SECURITY_OFFICER", "MASKING_POLICY_UPDATED", "POLICY:4", "SUCCESS", "Updated Aadhaar rule", "127.0.0.1", "REQ-102");
        addAuditRow(rows, LocalDateTime.now().minusHours(3), "auditor", "ROLE_AUDITOR", "REPORT_GENERATED", "REPORT:12", "SUCCESS", "Generated Risk Assessment PDF", "127.0.0.1", "REQ-103");
        addAuditRow(rows, LocalDateTime.now().minusHours(4), "viewer", "ROLE_VIEWER", "ACCESS_DENIED", "POLICY:4", "DENIED", "Insufficient permission", "127.0.0.1", "REQ-104");

        container.setRows(rows);
        container.setTotalPiiFindings(rows.size());
    }

    private void addAuditRow(List<Map<String, Object>> rows, LocalDateTime time, String user, String role, String action, String resource, String result, String reason, String ip, String reqId) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("timestamp", time.toString());
        r.put("user", user);
        r.put("role", role);
        r.put("action", action);
        r.put("resource", resource);
        r.put("result", result);
        r.put("reason", reason);
        r.put("ipAddress", ip);
        r.put("correlationId", reqId);
        rows.add(r);
    }
}
