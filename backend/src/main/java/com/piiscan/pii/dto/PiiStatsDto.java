package com.piiscan.pii.dto;

import lombok.Builder;
import lombok.Data;

/** KPI aggregates for the PII Detection dashboard */
@Data
@Builder
public class PiiStatsDto {
    private long totalDetected;
    private long critical;
    private long high;
    private long medium;
    private long low;
    private double avgConfidence;
}
