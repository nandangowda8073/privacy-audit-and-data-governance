package com.piiscan.risk.service;

import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskClassificationServiceTest {

    @InjectMocks
    private RiskClassificationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRiskLevelDetermination() {
        assertEquals(RiskLevel.LOW, service.determineRiskLevel(PiiEntityType.PERSON_NAME));
        assertEquals(RiskLevel.MEDIUM, service.determineRiskLevel(PiiEntityType.EMAIL));
        assertEquals(RiskLevel.HIGH, service.determineRiskLevel(PiiEntityType.PAN));
        assertEquals(RiskLevel.CRITICAL, service.determineRiskLevel(PiiEntityType.AADHAAR));
        assertEquals(RiskLevel.CRITICAL, service.determineRiskLevel(PiiEntityType.CREDIT_CARD));
    }

    @Test
    void testRiskScoreCalculation() {
        assertEquals(1, service.calculateRiskScore(RiskLevel.LOW));
        assertEquals(2, service.calculateRiskScore(RiskLevel.MEDIUM));
        assertEquals(3, service.calculateRiskScore(RiskLevel.HIGH));
        assertEquals(4, service.calculateRiskScore(RiskLevel.CRITICAL));
        assertEquals(0, service.calculateRiskScore(null));
    }
}
