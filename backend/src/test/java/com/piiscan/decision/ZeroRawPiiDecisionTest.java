package com.piiscan.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piiscan.auth.entity.Permission;
import com.piiscan.decision.dto.DecisionExplanationRequest;
import com.piiscan.decision.dto.ExplainableDecisionDto;
import com.piiscan.decision.enums.DecisionType;
import com.piiscan.decision.service.ExplainableDecisionService;
import com.piiscan.pii.entity.PiiEntity;
import com.piiscan.pii.enums.PiiCategory;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.pii.repository.PiiEntityRepository;

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
public class ZeroRawPiiDecisionTest {

    @Autowired
    private ExplainableDecisionService decisionService;

    @Autowired
    private PiiEntityRepository piiEntityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AADHAAR_RAW = "456789123456";
    private static final String PAN_RAW = "ABCDE1234F";
    private static final String CARD_RAW = "4532123412345678";

    private PiiEntity aadhaarEntity;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "sec_officer", "password",
                List.of(new SimpleGrantedAuthority("ROLE_SECURITY_OFFICER"),
                        new SimpleGrantedAuthority("DECISION_EXPLAIN"),
                        new SimpleGrantedAuthority("DECISION_VIEW"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        aadhaarEntity = piiEntityRepository.save(PiiEntity.builder()
                .entityType(PiiEntityType.AADHAAR)
                .category(PiiCategory.PERSONAL_IDENTITY)
                .originalValue(AADHAAR_RAW)
                .maskedValue("XXXXXXXX3456")
                .confidenceScore(0.98)
                .riskLevel(RiskLevel.CRITICAL)
                .source("users_db")
                .build());
    }

    @Test
    @DisplayName("Zero Raw PII Security Test: Explanations must NEVER expose raw PII strings")
    void testZeroRawPiiExposureInExplanations() throws Exception {
        // 1. Risk Explanation
        ExplainableDecisionDto riskDto = decisionService.explainDecision(
                DecisionExplanationRequest.builder()
                        .decisionType(DecisionType.RISK_CLASSIFICATION)
                        .piiEntityId(aadhaarEntity.getId())
                        .build());
        String riskJson = objectMapper.writeValueAsString(riskDto);
        assertFalse(riskJson.contains(AADHAAR_RAW), "Risk explanation leaked raw Aadhaar value!");
        assertFalse(riskJson.contains(PAN_RAW), "Risk explanation contained raw PAN value!");
        assertFalse(riskJson.contains(CARD_RAW), "Risk explanation contained raw Credit Card value!");

        // 2. Masking Explanation
        ExplainableDecisionDto maskDto = decisionService.explainDecision(
                DecisionExplanationRequest.builder()
                        .decisionType(DecisionType.MASKING)
                        .piiEntityId(aadhaarEntity.getId())
                        .build());
        String maskJson = objectMapper.writeValueAsString(maskDto);
        assertFalse(maskJson.contains(AADHAAR_RAW), "Masking explanation leaked raw Aadhaar value!");

        // 3. Access Explanation
        ExplainableDecisionDto accessDto = decisionService.explainDecision(
                DecisionExplanationRequest.builder()
                        .decisionType(DecisionType.ACCESS)
                        .requiredPermission(Permission.PII_VIEW)
                        .build());
        String accessJson = objectMapper.writeValueAsString(accessDto);
        assertFalse(accessJson.contains(AADHAAR_RAW), "Access explanation leaked raw Aadhaar value!");
        assertFalse(accessJson.contains(PAN_RAW));
        assertFalse(accessJson.contains(CARD_RAW));
    }
}
