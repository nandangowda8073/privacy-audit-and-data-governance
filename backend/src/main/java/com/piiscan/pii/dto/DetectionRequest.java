package com.piiscan.pii.dto;

import lombok.Data;

@Data
public class DetectionRequest {
    private String text;          // for direct text scan
    private String source;        // description of source
}
