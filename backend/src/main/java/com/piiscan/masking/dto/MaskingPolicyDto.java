package com.piiscan.masking.dto;

import com.piiscan.auth.entity.Role;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaskingPolicyDto {
    private Long id;
    private Role role;
    private PiiEntityType piiType;
    private RiskLevel riskLevel;
    private MaskingType maskingType;
    private String description;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
