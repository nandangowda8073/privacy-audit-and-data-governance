package com.piiscan.dashboard;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import com.piiscan.audit.repository.AuditLogRepository;
import com.piiscan.dashboard.dto.DashboardOverviewDto;
import com.piiscan.dashboard.service.DashboardService;
import com.piiscan.masking.entity.MaskingPolicy;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.masking.repository.MaskingPolicyRepository;
import com.piiscan.pii.entity.PiiEntity;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.pii.repository.PiiEntityRepository;
import com.piiscan.risk.entity.RiskAssessment;
import com.piiscan.risk.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DashboardAnalyticsServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private PiiEntityRepository piiEntityRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private MaskingPolicyRepository maskingPolicyRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        riskAssessmentRepository.deleteAll();
        piiEntityRepository.deleteAll();
        maskingPolicyRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("Verify Clean System State gives 100.0 Compliance Score")
    void testCleanStateComplianceScore() {
        DashboardOverviewDto overview = dashboardService.getOverview("LAST_30_DAYS", null, null);
        assertNotNull(overview);
        assertEquals(100.0, overview.getComplianceScore(), "Clean database must yield 100.0% Compliance Score");
    }

    @Test
    @DisplayName("Verify Compliance Score penalty calculations for Critical, High, Medium, Low findings and Policy Violations")
    void testComplianceScorePenalties() {
        PiiEntity pii1 = piiEntityRepository.save(PiiEntity.builder().entityType(PiiEntityType.PAN).originalValue("ABCDE1234F").build());
        PiiEntity pii2 = piiEntityRepository.save(PiiEntity.builder().entityType(PiiEntityType.AADHAAR).originalValue("123456789012").build());

        // 1 Critical finding (-5.0)
        riskAssessmentRepository.save(RiskAssessment.builder()
                .piiEntity(pii1)
                .piiType(PiiEntityType.PAN)
                .riskLevel(RiskLevel.CRITICAL)
                .riskScore(95)
                .reason("Unencrypted Critical PAN")
                .build());

        // 1 High finding (-2.0)
        riskAssessmentRepository.save(RiskAssessment.builder()
                .piiEntity(pii2)
                .piiType(PiiEntityType.AADHAAR)
                .riskLevel(RiskLevel.HIGH)
                .riskScore(75)
                .reason("High Risk Aadhaar")
                .build());

        // 1 Policy Violation (-10.0)
        maskingPolicyRepository.save(MaskingPolicy.builder()
                .role(com.piiscan.auth.entity.Role.ROLE_SECURITY_OFFICER)
                .piiType(PiiEntityType.PAN)
                .riskLevel(RiskLevel.HIGH)
                .maskingType(MaskingType.NONE)
                .enabled(true)
                .build());

        // Expected score = 100.0 - (5.0 + 2.0 + 10.0) = 83.0%
        DashboardOverviewDto overview = dashboardService.getOverview("LAST_30_DAYS", null, null);
        assertEquals(83.0, overview.getComplianceScore());
    }

    @Test
    @DisplayName("Verify Access Security Metrics are calculated separately and DO NOT affect Compliance Score")
    void testAccessSecurityMetricsSeparation() {
        auditLogRepository.save(AuditLog.builder()
                .action(AuditAction.AUTH_LOGIN)
                .result(AuditResult.SUCCESS)
                .username("user1")
                .build());

        auditLogRepository.save(AuditLog.builder()
                .action(AuditAction.ACCESS_DENIED)
                .result(AuditResult.DENIED)
                .username("unauthorized_user")
                .build());

        DashboardOverviewDto overview = dashboardService.getOverview("LAST_30_DAYS", null, null);
        assertNotNull(overview.getAccessSecurityMetrics());
        assertEquals(2, overview.getAccessSecurityMetrics().getTotalAccessAttempts());
        assertEquals(1, overview.getAccessSecurityMetrics().getAccessDeniedCount());
        assertEquals(50.0, overview.getAccessSecurityMetrics().getAccessDeniedRate());

        // Compliance score should still be 100.0 since no risk findings or policy violations exist
        assertEquals(100.0, overview.getComplianceScore());
    }

    @Test
    @DisplayName("Verify date range validation throws IllegalArgumentException if dateFrom > dateTo")
    void testInvalidDateRangeValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            dashboardService.getOverview("CUSTOM", LocalDate.now(), LocalDate.now().minusDays(5));
        });
    }
}
