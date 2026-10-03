package com.piiscan.pii.dto;

import com.piiscan.pii.enums.PiiCategory;
import lombok.Data;

@Data
public class CustomPatternDto {
    private String patternName;
    private PiiCategory category;
    private String regex;
    private String description;
    private boolean enabled;
}
