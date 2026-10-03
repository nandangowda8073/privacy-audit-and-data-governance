package com.piiscan.masking.repository;

import com.piiscan.auth.entity.Role;
import com.piiscan.masking.entity.MaskingPolicy;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaskingPolicyRepository extends JpaRepository<MaskingPolicy, Long> {

    Optional<MaskingPolicy> findByRoleAndPiiTypeAndRiskLevelAndEnabledTrue(
            Role role, PiiEntityType piiType, RiskLevel riskLevel);

    List<MaskingPolicy> findByEnabledTrue();

    List<MaskingPolicy> findByRole(Role role);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(m) FROM MaskingPolicy m WHERE m.riskLevel IN :riskLevels AND (m.enabled = false OR m.maskingType = :maskingType)")
    long countPolicyViolations(
            @org.springframework.data.repository.query.Param("riskLevels") java.util.List<RiskLevel> riskLevels,
            @org.springframework.data.repository.query.Param("maskingType") com.piiscan.masking.enums.MaskingType maskingType
    );
}
