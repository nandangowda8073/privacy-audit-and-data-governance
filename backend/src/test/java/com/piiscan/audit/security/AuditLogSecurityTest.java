package com.piiscan.audit.security;

import com.piiscan.audit.service.AuditLogService;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuditLogSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogService auditLogService;

    @BeforeEach
    public void setUp() {
        auditLogService.logAuthEvent(
                AuditAction.AUTH_LOGIN,
                AuditResult.SUCCESS,
                "test_user",
                "ROLE_SUPER_ADMIN",
                "Logged in for security tests",
                "Initial seed"
        );
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"AUDIT_LOG_VIEW", "AUDIT_LOG_EXPORT"})
    public void superAdmin_hasAccessToAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "officer", authorities = {"AUDIT_LOG_VIEW", "AUDIT_LOG_EXPORT"})
    public void securityOfficer_hasAccessToAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "auditor", authorities = {"AUDIT_LOG_VIEW", "AUDIT_LOG_EXPORT"})
    public void auditor_hasAccessToAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"DASHBOARD_VIEW", "RISK_VIEW"})
    public void viewer_isForbiddenToAccessAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void unauthenticated_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"AUDIT_LOG_VIEW", "AUDIT_LOG_EXPORT", "USER_MANAGE"})
    public void auditLogsAreImmutable_noPutOrDeleteEndpoints() throws Exception {
        // PUT endpoint should not exist — expect 4xx or 5xx (but not 2xx success)
        int putStatus = mockMvc.perform(put("/api/v1/audit-logs/1"))
                .andReturn().getResponse().getStatus();
        assertThat(putStatus).isNotIn(200, 201, 202, 204);

        // DELETE endpoint should not exist — expect 4xx or 5xx (but not 2xx success)
        int deleteStatus = mockMvc.perform(delete("/api/v1/audit-logs/1"))
                .andReturn().getResponse().getStatus();
        assertThat(deleteStatus).isNotIn(200, 201, 202, 204);
    }
}
