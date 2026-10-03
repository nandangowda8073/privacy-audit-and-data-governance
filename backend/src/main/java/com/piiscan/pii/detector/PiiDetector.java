package com.piiscan.pii.detector;

import com.piiscan.pii.dto.DetectionMatch;

import java.util.List;

/**
 * Strategy interface for all PII detectors.
 * Each detector is independently responsible for detecting
 * one or more PII entity types from raw text input.
 */
public interface PiiDetector {

    /**
     * Detect PII entities from the given text.
     *
     * @param text   raw input text to scan
     * @param source human-readable source label (e.g. "FILE:data.csv")
     * @return list of detected PII matches
     */
    List<DetectionMatch> detect(String text, String source);

    /**
     * Validate that the detector is correctly configured and ready to use.
     */
    boolean isAvailable();
}
