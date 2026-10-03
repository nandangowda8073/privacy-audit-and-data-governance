package com.piiscan.masking.service;

import com.piiscan.auth.entity.Role;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.masking.repository.MaskingPolicyRepository;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DataMaskingTests {

    @InjectMocks
    private DataMaskingService dataMaskingService;

    @Mock
    private MaskingPolicyRepository policyRepository;

    @InjectMocks
    private MaskingPolicyService policyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(policyRepository.findByRoleAndPiiTypeAndRiskLevelAndEnabledTrue(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty()); // Use defaults
    }

    // ── DataMaskingService Tests ───────────────────────────────────────────────

    @Test
    void aadhaarFullMask() {
        String result = dataMaskingService.applyMask("456789123456", PiiEntityType.AADHAAR, MaskingType.FULL);
        assertEquals("XXXXXXXXXXXX", result);
    }

    @Test
    void aadhaarPartialMask() {
        String result = dataMaskingService.applyMask("456789123456", PiiEntityType.AADHAAR, MaskingType.PARTIAL);
        assertEquals("XXXX XXXX 3456", result);
    }

    @Test
    void panPartialMask() {
        String result = dataMaskingService.applyMask("ABCDE1234F", PiiEntityType.PAN, MaskingType.PARTIAL);
        assertEquals("XXXXX1234X", result);
    }

    @Test
    void emailPartialMask() {
        String result = dataMaskingService.applyMask("nandan@gmail.com", PiiEntityType.EMAIL, MaskingType.PARTIAL);
        assertTrue(result.startsWith("n") && result.contains("@gmail.com"));
    }

    @Test
    void emailFullMask() {
        String original = "nandan@gmail.com";
        String result = dataMaskingService.applyMask(original, PiiEntityType.EMAIL, MaskingType.FULL);
        assertEquals(original.length(), result.length());
        assertFalse(result.contains("@"));
    }

    @Test
    void phonePartialMask() {
        String result = dataMaskingService.applyMask("9876543210", PiiEntityType.PHONE, MaskingType.PARTIAL);
        assertTrue(result.endsWith("3210") && result.startsWith("X"));
    }

    @Test
    void creditCardPartialMask() {
        String result = dataMaskingService.applyMask("4532123412345678", PiiEntityType.CREDIT_CARD, MaskingType.PARTIAL);
        assertTrue(result.endsWith("5678") && result.contains("XXXX"));
    }

    @Test
    void bankAccountPartialMask() {
        String result = dataMaskingService.applyMask("123456789012", PiiEntityType.BANK_ACCOUNT, MaskingType.PARTIAL);
        assertTrue(result.endsWith("9012") && result.startsWith("X"));
    }

    @Test
    void upiPartialMask() {
        String result = dataMaskingService.applyMask("nandan@upi", PiiEntityType.UPI_ID, MaskingType.PARTIAL);
        assertTrue(result.startsWith("n") && result.contains("@upi"));
    }

    @Test
    void addressPartialMask() {
        String result = dataMaskingService.applyMask("12 MG Road", PiiEntityType.ADDRESS, MaskingType.PARTIAL);
        assertTrue(result.startsWith("XX"));
    }

    @Test
    void nullValueHandled() {
        String result = dataMaskingService.applyMask(null, PiiEntityType.AADHAAR, MaskingType.FULL);
        assertNull(result);
    }

    @Test
    void emptyValueHandled() {
        String result = dataMaskingService.applyMask("", PiiEntityType.AADHAAR, MaskingType.FULL);
        assertEquals("", result);
    }

    @Test
    void noneTypeLeavesValueUnchanged() {
        String result = dataMaskingService.applyMask("456789123456", PiiEntityType.AADHAAR, MaskingType.NONE);
        assertEquals("456789123456", result);
    }

    // ── MaskingPolicyService Defaults Tests ──────────────────────────────────

    @Test
    void viewerCriticalIsFullMask() {
        MaskingType type = policyService.resolve(Role.ROLE_VIEWER, PiiEntityType.AADHAAR, RiskLevel.CRITICAL);
        assertEquals(MaskingType.FULL, type);
    }

    @Test
    void auditorCriticalIsFullMask() {
        MaskingType type = policyService.resolve(Role.ROLE_AUDITOR, PiiEntityType.AADHAAR, RiskLevel.CRITICAL);
        assertEquals(MaskingType.FULL, type);
    }

    @Test
    void securityOfficerCriticalIsPartial() {
        MaskingType type = policyService.resolve(Role.ROLE_SECURITY_OFFICER, PiiEntityType.AADHAAR, RiskLevel.CRITICAL);
        assertEquals(MaskingType.PARTIAL, type);
    }

    @Test
    void superAdminCriticalIsPartial() {
        MaskingType type = policyService.resolve(Role.ROLE_SUPER_ADMIN, PiiEntityType.AADHAAR, RiskLevel.CRITICAL);
        assertEquals(MaskingType.PARTIAL, type);
    }

    @Test
    void viewerMediumEmailIsPartial() {
        MaskingType type = policyService.resolve(Role.ROLE_VIEWER, PiiEntityType.EMAIL, RiskLevel.MEDIUM);
        assertEquals(MaskingType.PARTIAL, type);
    }

    @Test
    void securityOfficerMediumEmailIsNone() {
        MaskingType type = policyService.resolve(Role.ROLE_SECURITY_OFFICER, PiiEntityType.EMAIL, RiskLevel.MEDIUM);
        assertEquals(MaskingType.NONE, type);
    }
}
