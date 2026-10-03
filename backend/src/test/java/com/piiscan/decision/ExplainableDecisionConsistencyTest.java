package com.piiscan.decision;

import com.piiscan.auth.entity.Permission;
import com.piiscan.auth.entity.Role;
import com.piiscan.auth.entity.User;
import com.piiscan.auth.repository.UserRepository;
import com.piiscan.decision.dto.DecisionExplanationRequest;
import com.piiscan.decision.dto.ExplainableDecisionDto;
import com.piiscan.decision.enums.DecisionType;
import com.piiscan.decision.service.ExplainableDecisionService;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.masking.service.MaskingPolicyService;
import com.piiscan.pii.entity.PiiEntity;
import com.piiscan.pii.enums.PiiCategory;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ExplainableDecisionConsistencyTest {

    @Autowired
    private ExplainableDecisionService decisionService;

    @Autowired
    private PiiEntityRepository piiEntityRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private MaskingPolicyService maskingPolicyService;

    @Autowired
    private UserRepository userRepository;

    private PiiEntity testEntity;
    private RiskAssessment testAssessment;

    @BeforeEach
    void setUp() {
        // Authenticate Security Context as SUPER_ADMIN
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                        new SimpleGrantedAuthority("DECISION_EXPLAIN"),
                        new SimpleGrantedAuthority("DECISION_VIEW"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Create PII Entity & Risk Assessment
        testEntity = piiEntityRepository.save(PiiEntity.builder()
                .entityType(PiiEntityType.AADHAAR)
                .category(PiiCategory.PERSONAL_IDENTITY)
                .originalValue("456789123456")
                .maskedValue("XXXXXXXX3456")
                .confidenceScore(0.99)
                .riskLevel(RiskLevel.CRITICAL)
                .source("users_table")
                .build());

        testAssessment = riskAssessmentRepository.save(RiskAssessment.builder()
                .piiEntity(testEntity)
                .piiType(PiiEntityType.AADHAAR)
                .riskLevel(RiskLevel.CRITICAL)
                .riskScore(95)
                .reason("Aadhaar is a critical national identity document requiring mandatory protection")
                .build());
    }

    @Test
    @DisplayName("Risk Explanation output must match Module 7 RiskAssessment exactly")
    void testRiskExplanationConsistency() {
        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.RISK_CLASSIFICATION)
                .piiEntityId(testEntity.getId())
                .build();

        ExplainableDecisionDto dto = decisionService.explainDecision(req);

        assertNotNull(dto);
        assertEquals(DecisionType.RISK_CLASSIFICATION, dto.getDecisionType());
        assertEquals("CRITICAL", dto.getOutcome());
        assertEquals(testAssessment.getRiskScore(), dto.getRiskScore());
        assertEquals(testAssessment.getReason(), dto.getReason());
        assertTrue(dto.getFactors().stream().anyMatch(f -> f.contains("Aadhaar") || f.contains("identity")));
    }

    @Test
    @DisplayName("Masking Explanation output must match Module 8 MaskingPolicyService decision exactly")
    void testMaskingExplanationConsistency() {
        MaskingType expectedMasking = maskingPolicyService.resolve(Role.ROLE_SUPER_ADMIN, PiiEntityType.AADHAAR, RiskLevel.CRITICAL);

        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.MASKING)
                .piiEntityId(testEntity.getId())
                .build();

        ExplainableDecisionDto dto = decisionService.explainDecision(req);

        assertNotNull(dto);
        assertEquals(DecisionType.MASKING, dto.getDecisionType());
        assertEquals(expectedMasking.name(), dto.getOutcome());
        assertEquals("ROLE_SUPER_ADMIN", dto.getRole());
    }

    @Test
    @DisplayName("Access Explanation output must match Module 9 RBAC permission check exactly")
    void testAccessExplanationConsistency() {
        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.ACCESS)
                .requiredPermission(Permission.PII_VIEW)
                .build();

        ExplainableDecisionDto dto = decisionService.explainDecision(req);

        assertNotNull(dto);
        assertEquals(DecisionType.ACCESS, dto.getDecisionType());
        assertEquals("ALLOWED", dto.getOutcome());
        assertTrue(dto.getPermissionGranted());
    }
}
