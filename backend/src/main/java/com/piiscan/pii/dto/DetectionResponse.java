package com.piiscan.pii.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** Summary response returned by the detection endpoints */
@Data
@Builder
public class DetectionResponse {
    private int totalDetected;
    private long executionTimeMs;
    private String source;
    private List<PiiEntityDto> entities;

    // Breakdown counts
    private Map<String, Long> byCategory;
    private Map<String, Long> byRiskLevel;
    private Map<String, Long> byDetectionMethod;
}
