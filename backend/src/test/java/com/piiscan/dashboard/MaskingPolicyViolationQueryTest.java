package com.piiscan.dashboard;

import com.piiscan.auth.entity.Role;
import com.piiscan.masking.entity.MaskingPolicy;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.masking.repository.MaskingPolicyRepository;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class MaskingPolicyViolationQueryTest {

    @Autowired
    private MaskingPolicyRepository maskingPolicyRepository;

    @Test
    @DisplayName("Verify countPolicyViolations ONLY counts HIGH/CRITICAL disabled or NONE policies, NOT LOW/MEDIUM")
    void testCountPolicyViolationsPrecedence() {
        // 1. HIGH risk, NONE masking -> SHOULD BE COUNTED (Violation 1)
        maskingPolicyRepository.save(MaskingPolicy.builder()
                .role(Role.ROLE_SECURITY_OFFICER)
                .piiType(PiiEntityType.PAN)
                .riskLevel(RiskLevel.HIGH)
                .maskingType(MaskingType.NONE)
                .enabled(true)
                .build());

        // 2. CRITICAL risk, FULL masking, but DISABLED -> SHOULD BE COUNTED (Violation 2)
        maskingPolicyRepository.save(MaskingPolicy.builder()
                .role(Role.ROLE_SECURITY_OFFICER)
                .piiType(PiiEntityType.AADHAAR)
                .riskLevel(RiskLevel.CRITICAL)
                .maskingType(MaskingType.FULL)
                .enabled(false)
                .build());

        // 3. LOW risk, NONE masking -> SHOULD NOT BE COUNTED (Not high/critical)
        maskingPolicyRepository.save(MaskingPolicy.builder()
                .role(Role.ROLE_SECURITY_OFFICER)
                .piiType(PiiEntityType.EMAIL)
                .riskLevel(RiskLevel.LOW)
                .maskingType(MaskingType.NONE)
                .enabled(true)
                .build());

        // 4. MEDIUM risk, NONE masking -> SHOULD NOT BE COUNTED (Not high/critical)
        maskingPolicyRepository.save(MaskingPolicy.builder()
                .role(Role.ROLE_SECURITY_OFFICER)
                .piiType(PiiEntityType.PHONE)
                .riskLevel(RiskLevel.MEDIUM)
                .maskingType(MaskingType.NONE)
                .enabled(true)
                .build());

        // 5. HIGH risk, FULL masking, ENABLED -> SHOULD NOT BE COUNTED (Properly masked and enabled)
        maskingPolicyRepository.save(MaskingPolicy.builder()
                .role(Role.ROLE_SECURITY_OFFICER)
                .piiType(PiiEntityType.CREDIT_CARD)
                .riskLevel(RiskLevel.HIGH)
                .maskingType(MaskingType.FULL)
                .enabled(true)
                .build());

        long violations = maskingPolicyRepository.countPolicyViolations(
                List.of(RiskLevel.HIGH, RiskLevel.CRITICAL),
                MaskingType.NONE
        );

        assertEquals(2, violations, "Policy violations count must equal exactly 2 (HIGH/CRITICAL only)");
    }
}
