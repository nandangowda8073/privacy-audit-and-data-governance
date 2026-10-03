package com.piiscan.pii.engine;

import com.piiscan.pii.detector.CustomPatternDetector;
import com.piiscan.pii.detector.RegexDetector;
import com.piiscan.pii.dto.DetectionMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central orchestration engine for PII detection.
 *
 * Responsibilities:
 *  1. Execute all registered detectors against the input text
 *  2. Merge results from all detectors
 *  3. Deduplicate overlapping matches (same value + same type)
 *  4. Return a consolidated, cleaned detection result list
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PiiDetectionEngine {

    private final RegexDetector         regexDetector;
    private final CustomPatternDetector customPatternDetector;
    // Future: inject PresidioDetector here without changing existing code

    /**
     * Run all available detectors on a text chunk and return merged, deduplicated results.
     */
    public List<DetectionMatch> detectAll(String text, String source) {
        long start = System.currentTimeMillis();
        log.info("[PiiDetectionEngine] Starting detection on source '{}', text length={}", source, text == null ? 0 : text.length());

        List<DetectionMatch> allMatches = new ArrayList<>();

        // 1. Regex detector (always runs)
        if (regexDetector.isAvailable()) {
            allMatches.addAll(regexDetector.detect(text, source));
        }

        // 2. Custom pattern detector (runs if patterns exist)
        if (customPatternDetector.isAvailable()) {
            allMatches.addAll(customPatternDetector.detect(text, source));
        }

        // 3. Deduplicate
        List<DetectionMatch> deduped = deduplicate(allMatches);

        log.info("[PiiDetectionEngine] Completed. Raw={}, Deduped={}, time={}ms",
            allMatches.size(), deduped.size(), System.currentTimeMillis() - start);

        return deduped;
    }

    /**
     * Removes duplicate detections of the same value + entity type combination.
     * When duplicates exist, the one with the highest confidence score is retained.
     */
    private List<DetectionMatch> deduplicate(List<DetectionMatch> matches) {
        Map<String, DetectionMatch> unique = new LinkedHashMap<>();
        for (DetectionMatch m : matches) {
            String key = m.getEntityType() + ":" + m.getOriginalValue().toLowerCase();
            unique.merge(key, m, (existing, incoming) ->
                incoming.getConfidenceScore() > existing.getConfidenceScore() ? incoming : existing);
        }
        return new ArrayList<>(unique.values());
    }
}
