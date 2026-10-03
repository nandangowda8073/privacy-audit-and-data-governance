package com.piiscan.audit.service;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import com.piiscan.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuditLogServiceTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    public void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin_tester", roles = {"SUPER_ADMIN"})
    public void testAuthEventLogging() {
        AuditLog log = auditLogService.logAuthEvent(
                AuditAction.AUTH_LOGIN,
                AuditResult.SUCCESS,
                "admin_tester",
                "ROLE_SUPER_ADMIN",
                "User logged in successfully",
                "Valid credentials"
        );

        assertThat(log).isNotNull();
        assertThat(log.getId()).isNotNull();
        assertThat(log.getUsername()).isEqualTo("admin_tester");
        assertThat(log.getAction()).isEqualTo(AuditAction.AUTH_LOGIN);
        assertThat(log.getResult()).isEqualTo(AuditResult.SUCCESS);
    }

    @Test
    @WithMockUser(username = "officer_tester", roles = {"SECURITY_OFFICER"})
    public void testDatabaseEventLogging() {
        AuditLog log = auditLogService.logDatabaseEvent(
                AuditAction.DATABASE_CREATE,
                AuditResult.SUCCESS,
                101L,
                "Customer_Production_DB",
                "Created new MySQL connection",
                "New DB onboarded"
        );

        assertThat(log).isNotNull();
        assertThat(log.getDatabaseId()).isEqualTo(101L);
        assertThat(log.getDatabaseName()).isEqualTo("Customer_Production_DB");
        assertThat(log.getAction()).isEqualTo(AuditAction.DATABASE_CREATE);
    }

    @Test
    @WithMockUser(username = "auditor_tester", roles = {"AUDITOR"})
    public void testPiiAndRiskEventLogging() {
        AuditLog piiLog = auditLogService.logPiiEvent(
                AuditAction.PII_DETECTED,
                AuditResult.SUCCESS,
                "SCAN-8899",
                "HR_Vault",
                "employees",
                "AADHAAR",
                "CRITICAL",
                25
        );

        assertThat(piiLog).isNotNull();
        assertThat(piiLog.getPiiType()).isEqualTo("AADHAAR");
        assertThat(piiLog.getRiskLevel()).isEqualTo("CRITICAL");

        AuditLog riskLog = auditLogService.logRiskEvent(
                AuditAction.RISK_CLASSIFIED,
                AuditResult.SUCCESS,
                "SCAN-8899",
                "AADHAAR",
                "CRITICAL",
                "Aadhaar is a critical national identifier"
        );

        assertThat(riskLog).isNotNull();
        assertThat(riskLog.getAction()).isEqualTo(AuditAction.RISK_CLASSIFIED);
    }

    @Test
    @WithMockUser(username = "admin_tester", roles = {"SUPER_ADMIN"})
    public void testFilteringAndPagination() {
        auditLogService.logAuthEvent(AuditAction.AUTH_LOGIN, AuditResult.SUCCESS, "user1", "ROLE_VIEWER", "Login 1", "OK");
        auditLogService.logAuthEvent(AuditAction.AUTH_LOGIN_FAILED, AuditResult.FAILURE, "user2", "ROLE_VIEWER", "Failed Login", "Bad Password");
        auditLogService.logDatabaseEvent(AuditAction.DATABASE_DELETE, AuditResult.DENIED, 55L, "HR_DB", "Delete attempted", "Denied permission");

        Page<AuditLog> resultPage = auditLogService.getAuditLogs(
                null, AuditResult.DENIED, null, null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getDatabaseName()).isEqualTo("HR_DB");
    }

    @Test
    @WithMockUser(username = "auditor_tester", roles = {"AUDITOR"})
    public void testCsvExportGeneration() {
        auditLogService.logAuthEvent(AuditAction.AUTH_LOGIN, AuditResult.SUCCESS, "exporter", "ROLE_AUDITOR", "Logged in for export test", "OK");

        byte[] csvData = auditLogService.exportAuditLogsCsv(null, null, null, null, null, null, null, null);
        assertThat(csvData).isNotEmpty();
        String csvText = new String(csvData);
        assertThat(csvText).contains("ID,Timestamp,Request ID,User,Role,Action,Result");
        assertThat(csvText).contains("exporter");
        assertThat(csvText).contains("AUTH_LOGIN");
    }
}
