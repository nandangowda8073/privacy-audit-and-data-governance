package com.piiscan.risk.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskSummaryDto {
    private long low;
    private long medium;
    private long high;
    private long critical;
}
