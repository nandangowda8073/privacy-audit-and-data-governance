package com.piiscan.risk.repository;

import com.piiscan.risk.entity.RiskAssessment;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    List<RiskAssessment> findByRiskLevel(RiskLevel riskLevel);

    List<RiskAssessment> findByPiiType(PiiEntityType piiType);

    @Query("SELECT r FROM RiskAssessment r WHERE r.piiEntity.source = :source")
    List<RiskAssessment> findBySource(String source);

    long countByRiskLevel(RiskLevel riskLevel);

    long countByRiskLevelAndCreatedAtBetween(RiskLevel riskLevel, java.time.LocalDateTime from, java.time.LocalDateTime to);

    @Query("SELECT r.piiType, COUNT(r) FROM RiskAssessment r WHERE r.createdAt BETWEEN :from AND :to GROUP BY r.piiType")
    List<Object[]> countByPiiTypeGrouped(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from, @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    @Query("SELECT r.riskLevel, COUNT(r) FROM RiskAssessment r WHERE r.createdAt BETWEEN :from AND :to GROUP BY r.riskLevel")
    List<Object[]> countByRiskLevelGrouped(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from, @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);
}
