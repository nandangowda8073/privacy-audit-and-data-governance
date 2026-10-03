package com.piiscan.pii.detector;

import com.piiscan.pii.dto.DetectionMatch;
import com.piiscan.pii.enums.DetectionMethod;
import com.piiscan.pii.enums.PiiCategory;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.pii.service.PiiMaskingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex-based PII detector.
 * Uses curated, production-grade regular expressions for Indian and global
 * PII entity types. Confidence is always 1.0 for a complete regex match.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RegexDetector implements PiiDetector {

    private final PiiMaskingService maskingService;

    // ── Regex patterns ────────────────────────────────────────────────────────

    private static final Map<PiiEntityType, String> PATTERNS = Map.ofEntries(
        // Indian PII
        Map.entry(PiiEntityType.AADHAAR,
            "\\b[2-9]{1}[0-9]{3}\\s?[0-9]{4}\\s?[0-9]{4}\\b"),
        Map.entry(PiiEntityType.PAN,
            "\\b[A-Z]{5}[0-9]{4}[A-Z]{1}\\b"),
        Map.entry(PiiEntityType.PASSPORT,
            "\\b[A-Z]{1}[0-9]{7}\\b"),
        Map.entry(PiiEntityType.DRIVING_LICENSE,
            "\\b(([A-Z]{2}[0-9]{2})( )|([A-Z]{2}-[0-9]{2}))[0-9]{4}[0-9]{7}\\b"),
        Map.entry(PiiEntityType.CREDIT_CARD,
            "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\\b"),
        Map.entry(PiiEntityType.DEBIT_CARD,
            "\\b(?:6[0-9]{15}|5[0-9]{15})\\b"),
        Map.entry(PiiEntityType.BANK_ACCOUNT,
            "\\b[0-9]{9,18}\\b"),
        Map.entry(PiiEntityType.IFSC,
            "\\b[A-Z]{4}0[A-Z0-9]{6}\\b"),
        Map.entry(PiiEntityType.UPI_ID,
            "\\b[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}\\b"),
        Map.entry(PiiEntityType.PIN_CODE,
            "\\b[1-9][0-9]{5}\\b"),

        // Contact
        Map.entry(PiiEntityType.EMAIL,
            "\\b[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}\\b"),
        Map.entry(PiiEntityType.PHONE,
            "\\b(?:\\+91|91)?[6-9][0-9]{9}\\b"),

        // Personal info
        Map.entry(PiiEntityType.DATE_OF_BIRTH,
            "\\b(?:0?[1-9]|[12][0-9]|3[01])[/-](?:0?[1-9]|1[0-2])[/-](?:19|20)[0-9]{2}\\b")
    );

    // ── Category + Risk look-ups ───────────────────────────────────────────────

    private static final Map<PiiEntityType, PiiCategory> CATEGORY_MAP = Map.ofEntries(
        Map.entry(PiiEntityType.AADHAAR,         PiiCategory.PERSONAL_IDENTITY),
        Map.entry(PiiEntityType.PAN,             PiiCategory.PERSONAL_IDENTITY),
        Map.entry(PiiEntityType.PASSPORT,        PiiCategory.PERSONAL_IDENTITY),
        Map.entry(PiiEntityType.DRIVING_LICENSE, PiiCategory.PERSONAL_IDENTITY),
        Map.entry(PiiEntityType.CREDIT_CARD,     PiiCategory.FINANCIAL),
        Map.entry(PiiEntityType.DEBIT_CARD,      PiiCategory.FINANCIAL),
        Map.entry(PiiEntityType.BANK_ACCOUNT,    PiiCategory.FINANCIAL),
        Map.entry(PiiEntityType.IFSC,            PiiCategory.FINANCIAL),
        Map.entry(PiiEntityType.UPI_ID,          PiiCategory.FINANCIAL),
        Map.entry(PiiEntityType.EMAIL,           PiiCategory.CONTACT),
        Map.entry(PiiEntityType.PHONE,           PiiCategory.CONTACT),
        Map.entry(PiiEntityType.PIN_CODE,        PiiCategory.CONTACT),
        Map.entry(PiiEntityType.DATE_OF_BIRTH,   PiiCategory.PERSONAL_INFORMATION)
    );

    private static final Map<PiiEntityType, RiskLevel> RISK_MAP = Map.ofEntries(
        Map.entry(PiiEntityType.AADHAAR,         RiskLevel.CRITICAL),
        Map.entry(PiiEntityType.PAN,             RiskLevel.CRITICAL),
        Map.entry(PiiEntityType.PASSPORT,        RiskLevel.CRITICAL),
        Map.entry(PiiEntityType.DRIVING_LICENSE, RiskLevel.CRITICAL),
        Map.entry(PiiEntityType.CREDIT_CARD,     RiskLevel.CRITICAL),
        Map.entry(PiiEntityType.DEBIT_CARD,      RiskLevel.CRITICAL),
        Map.entry(PiiEntityType.BANK_ACCOUNT,    RiskLevel.CRITICAL),
        Map.entry(PiiEntityType.IFSC,            RiskLevel.HIGH),
        Map.entry(PiiEntityType.UPI_ID,          RiskLevel.HIGH),
        Map.entry(PiiEntityType.EMAIL,           RiskLevel.MEDIUM),
        Map.entry(PiiEntityType.PHONE,           RiskLevel.MEDIUM),
        Map.entry(PiiEntityType.PIN_CODE,        RiskLevel.LOW),
        Map.entry(PiiEntityType.DATE_OF_BIRTH,   RiskLevel.LOW)
    );

    // ── Detection logic ────────────────────────────────────────────────────────

    @Override
    public List<DetectionMatch> detect(String text, String source) {
        List<DetectionMatch> results = new ArrayList<>();
        if (text == null || text.isBlank()) return results;

        PATTERNS.forEach((entityType, regex) -> {
            try {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String value = matcher.group().trim();
                    results.add(DetectionMatch.builder()
                        .entityType(entityType)
                        .category(CATEGORY_MAP.getOrDefault(entityType, PiiCategory.CUSTOM))
                        .riskLevel(RISK_MAP.getOrDefault(entityType, RiskLevel.LOW))
                        .detectionMethod(DetectionMethod.REGEX)
                        .originalValue(value)
                        .maskedValue(maskingService.mask(value, entityType))
                        .confidenceScore(1.0)
                        .startIndex(matcher.start())
                        .endIndex(matcher.end())
                        .build());
                }
            } catch (Exception e) {
                log.warn("Regex error for entity type {}: {}", entityType, e.getMessage());
            }
        });

        log.info("RegexDetector found {} entities in source '{}'", results.size(), source);
        return results;
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available – no external dependency
    }
}
