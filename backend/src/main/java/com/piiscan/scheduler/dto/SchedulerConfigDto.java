package com.piiscan.scheduler.dto;

import com.piiscan.scheduler.entity.ScanType;
import lombok.Data;
import java.time.LocalTime;

@Data
public class SchedulerConfigDto {
    private boolean enabled;
    private LocalTime scanTime;
    private ScanType scanType;
    private boolean retryEnabled;
    private int retryCount;
    private int retryDelay;
    private boolean emailNotificationEnabled;
}
