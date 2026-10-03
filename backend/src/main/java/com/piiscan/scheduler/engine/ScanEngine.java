package com.piiscan.scheduler.engine;

import com.piiscan.scheduler.entity.SchedulerConfiguration;
import com.piiscan.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScanEngine {

    private final SchedulerService schedulerService;

    // Run every minute to check if it's time to scan
    @Scheduled(cron = "0 * * * * *")
    public void checkAndRunSchedule() {
        SchedulerConfiguration config = schedulerService.getConfig();
        
        if (!config.isEnabled()) {
            return;
        }

        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalTime scanTime = config.getScanTime().truncatedTo(ChronoUnit.MINUTES);

        if (now.equals(scanTime)) {
            log.info("Scheduled scan time reached. Triggering scan...");
            schedulerService.executeScan(false, "SYSTEM_SCHEDULER");
        }
    }
}
