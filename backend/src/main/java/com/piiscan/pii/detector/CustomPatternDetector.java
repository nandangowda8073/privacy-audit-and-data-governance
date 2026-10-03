package com.piiscan.pii.detector;

import com.piiscan.pii.dto.DetectionMatch;
import com.piiscan.pii.entity.CustomPattern;
import com.piiscan.pii.enums.DetectionMethod;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.pii.repository.CustomPatternRepository;
import com.piiscan.pii.service.PiiMaskingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Pattern detector.
 * Loads administrator-defined regex patterns from the database at runtime.
 * Supports adding / editing patterns without any code change (Open/Closed Principle).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPatternDetector implements PiiDetector {

    private final CustomPatternRepository customPatternRepository;
    private final PiiMaskingService maskingService;

    @Override
    public List<DetectionMatch> detect(String text, String source) {
        List<DetectionMatch> results = new ArrayList<>();
        if (text == null || text.isBlank()) return results;

        List<CustomPattern> activePatterns = customPatternRepository.findByEnabledTrue();

        for (CustomPattern cp : activePatterns) {
            try {
                Pattern pattern = Pattern.compile(cp.getRegex());
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String value = matcher.group().trim();
                    results.add(DetectionMatch.builder()
                        .entityType(PiiEntityType.CUSTOM)
                        .category(cp.getCategory())
                        .riskLevel(RiskLevel.MEDIUM)      // Default for custom patterns
                        .detectionMethod(DetectionMethod.CUSTOM_PATTERN)
                        .originalValue(value)
                        .maskedValue(maskingService.mask(value, PiiEntityType.CUSTOM))
                        .confidenceScore(0.91)             // Fixed confidence for custom
                        .startIndex(matcher.start())
                        .endIndex(matcher.end())
                        .build());
                }
            } catch (Exception e) {
                log.warn("CustomPattern '{}' failed: {}", cp.getPatternName(), e.getMessage());
            }
        }

        log.info("CustomPatternDetector found {} entities in source '{}'", results.size(), source);
        return results;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
