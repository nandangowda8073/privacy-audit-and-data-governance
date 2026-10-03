package com.piiscan.masking.dto;

import com.piiscan.masking.enums.MaskingType;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaskingPreviewRequest {
    private PiiEntityType piiType;
    private String value;
}
