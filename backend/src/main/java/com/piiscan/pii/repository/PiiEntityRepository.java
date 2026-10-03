package com.piiscan.pii.repository;

import com.piiscan.pii.entity.PiiEntity;
import com.piiscan.pii.enums.PiiCategory;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PiiEntityRepository extends JpaRepository<PiiEntity, Long> {
    List<PiiEntity> findTop50ByOrderByCreatedAtDesc();
    List<PiiEntity> findByCategory(PiiCategory category);
    List<PiiEntity> findByRiskLevel(RiskLevel riskLevel);
    List<PiiEntity> findByEntityType(PiiEntityType entityType);

    @Query("SELECT COUNT(p) FROM PiiEntity p WHERE p.riskLevel = 'CRITICAL'")
    long countCritical();

    @Query("SELECT COUNT(p) FROM PiiEntity p WHERE p.riskLevel = 'HIGH'")
    long countHigh();

    @Query("SELECT COUNT(p) FROM PiiEntity p WHERE p.riskLevel = 'MEDIUM'")
    long countMedium();

    @Query("SELECT COUNT(p) FROM PiiEntity p WHERE p.riskLevel = 'LOW'")
    long countLow();
}
