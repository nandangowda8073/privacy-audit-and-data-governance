package com.piiscan.scheduler.dto;

import com.piiscan.scheduler.entity.ScanStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class SchedulerStatusDto {
    private boolean enabled;
    private LocalTime nextScanTime;
    private LocalDateTime lastScanTime;
    private ScanStatus currentStatus;
    private Long currentJobId;
}
