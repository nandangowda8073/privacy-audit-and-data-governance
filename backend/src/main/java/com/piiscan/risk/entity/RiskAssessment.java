package com.piiscan.risk.entity;

import com.piiscan.pii.entity.PiiEntity;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pii_entity_id", nullable = false)
    private PiiEntity piiEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PiiEntityType piiType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(nullable = false)
    private Integer riskScore;

    @Column(length = 500, nullable = false)
    private String reason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
