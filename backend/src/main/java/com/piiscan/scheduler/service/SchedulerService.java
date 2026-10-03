package com.piiscan.scheduler.service;

import com.piiscan.database.repository.DatabaseConnectionRepository;
import com.piiscan.scheduler.dto.SchedulerConfigDto;
import com.piiscan.scheduler.dto.SchedulerStatusDto;
import com.piiscan.scheduler.entity.ScanStatus;
import com.piiscan.scheduler.entity.ScanType;
import com.piiscan.scheduler.entity.ScheduledScanHistory;
import com.piiscan.scheduler.entity.SchedulerConfiguration;
import com.piiscan.scheduler.exception.SchedulerException;
import com.piiscan.scheduler.repository.ScheduledScanHistoryRepository;
import com.piiscan.scheduler.repository.SchedulerConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final SchedulerConfigurationRepository configRepository;
    private final ScheduledScanHistoryRepository historyRepository;
    private final DatabaseConnectionRepository databaseConnectionRepository;
    private final com.piiscan.audit.service.AuditLogService auditLogService;

    private boolean isRunning = false;
    private Long currentJobId = null;

    public SchedulerConfiguration getConfig() {
        return configRepository.findAll().stream().findFirst().orElseGet(() -> {
            SchedulerConfiguration defaultConfig = new SchedulerConfiguration();
            defaultConfig.setEnabled(false);
            defaultConfig.setScanTime(LocalTime.of(2, 0));
            defaultConfig.setScanType(ScanType.FULL);
            defaultConfig.setRetryEnabled(true);
            defaultConfig.setRetryCount(3);
            defaultConfig.setRetryDelay(10);
            defaultConfig.setEmailNotificationEnabled(false);
            return configRepository.save(defaultConfig);
        });
    }

    public SchedulerConfiguration updateConfig(SchedulerConfigDto dto) {
        SchedulerConfiguration config = getConfig();
        config.setEnabled(dto.isEnabled());
        config.setScanTime(dto.getScanTime());
        config.setScanType(dto.getScanType());
        config.setRetryEnabled(dto.isRetryEnabled());
        config.setRetryCount(dto.getRetryCount());
        config.setRetryDelay(dto.getRetryDelay());
        config.setEmailNotificationEnabled(dto.isEmailNotificationEnabled());
        return configRepository.save(config);
    }

    public SchedulerStatusDto getStatus() {
        SchedulerConfiguration config = getConfig();
        ScheduledScanHistory lastScan = historyRepository.findFirstByOrderByStartedAtDesc();
        
        SchedulerStatusDto status = new SchedulerStatusDto();
        status.setEnabled(config.isEnabled());
        status.setNextScanTime(config.getScanTime());
        status.setLastScanTime(lastScan != null ? lastScan.getCompletedAt() : null);
        status.setCurrentStatus(isRunning ? ScanStatus.RUNNING : (lastScan != null ? lastScan.getStatus() : null));
        status.setCurrentJobId(currentJobId);
        
        return status;
    }

    public void setEnabled(boolean enabled) {
        SchedulerConfiguration config = getConfig();
        config.setEnabled(enabled);
        configRepository.save(config);
    }

    public List<ScheduledScanHistory> getHistory() {
        return historyRepository.findTop10ByOrderByStartedAtDesc();
    }

    @Async
    public void executeScan(boolean isManual, String triggeredBy) {
        if (isRunning) {
            log.warn("Scan is already running. Ignoring trigger.");
            return;
        }

        SchedulerConfiguration config = getConfig();
        isRunning = true;
        
        ScheduledScanHistory history = new ScheduledScanHistory();
        history.setScanType(isManual ? ScanType.FULL : config.getScanType());
        history.setStartedAt(LocalDateTime.now());
        history.setStatus(ScanStatus.RUNNING);
        history.setTriggeredBy(triggeredBy);
        history.setRetryCount(0);
        
        history = historyRepository.save(history);
        currentJobId = history.getId();

        try {
            log.info("Starting {} scan triggered by {}", history.getScanType(), triggeredBy);

            if (auditLogService != null) {
                com.piiscan.audit.enums.AuditAction startAction = isManual ? com.piiscan.audit.enums.AuditAction.SCAN_STARTED : com.piiscan.audit.enums.AuditAction.SCAN_SCHEDULED;
                auditLogService.logScanEvent(
                    startAction,
                    com.piiscan.audit.enums.AuditResult.SUCCESS,
                    String.valueOf(history.getId()),
                    null,
                    "ALL_ENABLED_DBS",
                    "ALL",
                    "Scan started: " + history.getScanType() + " triggered by " + triggeredBy,
                    "Scan initiated"
                );
            }
            
            // Mocking the scan logic since PII engine is not built yet
            long dbCount = databaseConnectionRepository.count();
            
            // Simulate processing time
            Thread.sleep(5000); // 5 seconds
            
            history.setDatabasesScanned((int) dbCount);
            history.setTablesScanned((int) (dbCount * 5)); // Mock 5 tables per DB
            history.setRecordsScanned(dbCount * 5000L); // Mock 5000 records per DB
            history.setPiiDetected(dbCount * 12L); // Mock some PII findings
            
            history.setStatus(ScanStatus.COMPLETED);
            log.info("Scan completed successfully.");

            if (auditLogService != null) {
                auditLogService.logScanEvent(
                    com.piiscan.audit.enums.AuditAction.SCAN_COMPLETED,
                    com.piiscan.audit.enums.AuditResult.SUCCESS,
                    String.valueOf(history.getId()),
                    null,
                    "ALL_ENABLED_DBS",
                    "ALL",
                    String.format("Scan completed. Scanned %d DBs, %d tables, %d records. Detected %d PII findings.",
                        history.getDatabasesScanned(), history.getTablesScanned(), history.getRecordsScanned(), history.getPiiDetected()),
                    "Completed without errors"
                );
            }
            
        } catch (Exception e) {
            log.error("Scan failed: ", e);
            history.setStatus(ScanStatus.FAILED);
            history.setFailedReason(e.getMessage());
            
            if (auditLogService != null) {
                auditLogService.logScanEvent(
                    com.piiscan.audit.enums.AuditAction.SCAN_FAILED,
                    com.piiscan.audit.enums.AuditResult.FAILURE,
                    String.valueOf(history.getId()),
                    null,
                    "ALL_ENABLED_DBS",
                    "ALL",
                    "Scan failed for job " + history.getId(),
                    e.getMessage()
                );
            }

            // Basic retry logic simulation could be added here or via @Retryable
            if (!isManual && config.isRetryEnabled()) {
                // To be implemented: actual retry scheduling
                history.setFailedReason(e.getMessage() + " (Will retry later)");
            }
        } finally {
            history.setCompletedAt(LocalDateTime.now());
            history.setDuration(java.time.Duration.between(history.getStartedAt(), history.getCompletedAt()).getSeconds());
            historyRepository.save(history);
            isRunning = false;
            currentJobId = null;
        }
    }
}
