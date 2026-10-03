package com.piiscan.decision;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import com.piiscan.audit.repository.AuditLogRepository;
import com.piiscan.decision.dto.DecisionExplanationRequest;
import com.piiscan.decision.dto.ExplainableDecisionDto;
import com.piiscan.decision.enums.DecisionType;
import com.piiscan.decision.service.ExplainableDecisionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ExplainableDecisionHistoricalTest {

    @Autowired
    private ExplainableDecisionService decisionService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "auditor", "password",
                List.of(new SimpleGrantedAuthority("ROLE_AUDITOR"),
                        new SimpleGrantedAuthority("DECISION_EXPLAIN"),
                        new SimpleGrantedAuthority("DECISION_VIEW"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Historical explanation MUST use captured audit metadata and NOT recalculate current policy")
    void testHistoricalExplanationUsesCapturedMetadata() {
        // Save historical audit log representing Policy A decision
        AuditLog historicalLog = auditLogRepository.save(AuditLog.builder()
                .username("auditor01")
                .role("ROLE_AUDITOR")
                .action(AuditAction.MASKING_APPLIED)
                .result(AuditResult.SUCCESS)
                .piiType("AADHAAR")
                .riskLevel("CRITICAL")
                .maskingType("FULL")
                .reason("Captured Historical Policy A - Full Masking")
                .requestId("REQ-HIST-1001")
                .timestamp(LocalDateTime.now().minusDays(30))
                .build());

        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.MASKING)
                .auditLogId(historicalLog.getId())
                .build();

        ExplainableDecisionDto dto = decisionService.explainDecision(req);

        assertNotNull(dto);
        assertTrue(dto.isHistorical());
        assertEquals("FULL", dto.getOutcome());
        assertEquals("Captured Historical Policy A - Full Masking", dto.getReason());
        assertEquals("REQ-HIST-1001", dto.getCorrelationId());
    }

    @Test
    @DisplayName("Historical explanation for log with missing metadata MUST return HISTORICAL_EXPLANATION_UNAVAILABLE")
    void testHistoricalExplanationUnavailableForIncompleteMetadata() {
        AuditLog incompleteLog = auditLogRepository.save(AuditLog.builder()
                .username("system")
                .role("ROLE_SYSTEM")
                .action(AuditAction.AUTH_LOGIN)
                .result(AuditResult.SUCCESS)
                .timestamp(LocalDateTime.now().minusDays(5))
                .build());

        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.ACCESS)
                .auditLogId(incompleteLog.getId())
                .build();

        ExplainableDecisionDto dto = decisionService.explainDecision(req);

        assertNotNull(dto);
        assertTrue(dto.isHistorical());
        assertEquals("HISTORICAL_EXPLANATION_UNAVAILABLE", dto.getOutcome());
        assertTrue(dto.getReason().contains("insufficient"));
    }
}
