package com.piiscan.risk.service;

import com.piiscan.pii.entity.PiiEntity;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.risk.entity.RiskAssessment;
import com.piiscan.risk.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskClassificationService {

    private final RiskAssessmentRepository riskAssessmentRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.piiscan.audit.service.AuditLogService auditLogService;

    /**
     * Centralized risk classification logic.
     * Evaluates the finding, determines risk, calculates score, generates explanation, and persists.
     */
    @Transactional
    public RiskAssessment classifyAndSave(PiiEntity piiEntity) {
        if (piiEntity == null || piiEntity.getEntityType() == null) {
            throw new IllegalArgumentException("Cannot classify risk for a null entity or null entity type.");
        }

        PiiEntityType type = piiEntity.getEntityType();
        RiskLevel riskLevel = determineRiskLevel(type);
        int score = calculateRiskScore(riskLevel);
        String reason = generateExplanation(type, riskLevel);

        RiskAssessment assessment = RiskAssessment.builder()
                .piiEntity(piiEntity)
                .piiType(type)
                .riskLevel(riskLevel)
                .riskScore(score)
                .reason(reason)
                .build();

        log.debug("Classified {} as {} (Score: {})", type, riskLevel, score);
        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        if (auditLogService != null) {
            auditLogService.logRiskEvent(
                com.piiscan.audit.enums.AuditAction.RISK_CLASSIFIED,
                com.piiscan.audit.enums.AuditResult.SUCCESS,
                piiEntity.getSource(),
                type.name(),
                riskLevel.name(),
                reason
            );
        }

        return saved;
    }

    public RiskLevel determineRiskLevel(PiiEntityType type) {
        switch (type) {
            case PERSON_NAME:
            case CITY:
            case COUNTRY:
            case PIN_CODE:
                return RiskLevel.LOW;
            case EMAIL:
            case PHONE:
            case ADDRESS:
            case DATE_OF_BIRTH:
                return RiskLevel.MEDIUM;
            case PAN:
            case PASSPORT:
            case DRIVING_LICENSE:
            case BANK_ACCOUNT:
            case IFSC:
                return RiskLevel.HIGH;
            case AADHAAR:
            case CREDIT_CARD:
            case DEBIT_CARD:
            case UPI_ID:
                return RiskLevel.CRITICAL;
            case CUSTOM:
            default:
                return RiskLevel.MEDIUM; // Default fallback
        }
    }

    public int calculateRiskScore(RiskLevel level) {
        if (level == null) return 0;
        switch (level) {
            case LOW: return 1;
            case MEDIUM: return 2;
            case HIGH: return 3;
            case CRITICAL: return 4;
            default: return 0;
        }
    }

    public String generateExplanation(PiiEntityType type, RiskLevel level) {
        switch (type) {
            case PERSON_NAME:
                return "Name is personally identifiable information with relatively low sensitivity.";
            case EMAIL:
                return "Email is personally identifiable information and can be used to identify or contact an individual.";
            case PAN:
                return "PAN is a government-issued financial identity identifier and requires high protection.";
            case AADHAAR:
                return "Aadhaar is a highly sensitive government identity identifier and requires critical protection.";
            case CREDIT_CARD:
                return "Credit card information is highly sensitive financial data and may enable financial fraud.";
            case PHONE:
                return "Phone number can be used for direct communication and requires moderate protection.";
            case BANK_ACCOUNT:
                return "Bank account information is highly sensitive and requires strict protection measures.";
            case PASSPORT:
                return "Passport is a critical government-issued travel and identity document.";
            default:
                return "Detected " + type + " which is classified as " + level + " risk.";
        }
    }
}
