package com.piiscan.masking.dto;

import com.piiscan.masking.enums.MaskingType;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaskingPreviewResponse {
    private PiiEntityType piiType;
    private RiskLevel riskLevel;
    private MaskingType maskingType;
    private String maskedValue;
    private String reason;
}
