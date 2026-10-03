package com.piiscan.scheduler.controller;

import com.piiscan.scheduler.dto.SchedulerConfigDto;
import com.piiscan.scheduler.dto.SchedulerStatusDto;
import com.piiscan.scheduler.entity.ScheduledScanHistory;
import com.piiscan.scheduler.entity.SchedulerConfiguration;
import com.piiscan.scheduler.service.SchedulerService;
import com.piiscan.platform.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('SCAN_VIEW')")
    public ResponseEntity<ApiResponse<SchedulerStatusDto>> getStatus() {
        return ResponseEntity.ok(ApiResponse.success("Scheduler status retrieved", schedulerService.getStatus()));
    }

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('SCAN_SCHEDULE')")
    public ResponseEntity<ApiResponse<Void>> startScheduler() {
        schedulerService.setEnabled(true);
        return ResponseEntity.ok(ApiResponse.success("Scheduler started", null));
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAuthority('SCAN_SCHEDULE')")
    public ResponseEntity<ApiResponse<Void>> stopScheduler() {
        schedulerService.setEnabled(false);
        return ResponseEntity.ok(ApiResponse.success("Scheduler stopped", null));
    }

    @PostMapping("/run")
    @PreAuthorize("hasAuthority('SCAN_START')")
    public ResponseEntity<ApiResponse<Void>> triggerManualScan(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "UNKNOWN";
        schedulerService.executeScan(true, username);
        return ResponseEntity.ok(ApiResponse.success("Manual scan triggered", null));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('SCAN_VIEW')")
    public ResponseEntity<ApiResponse<List<ScheduledScanHistory>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.success("Scheduler history retrieved", schedulerService.getHistory()));
    }

    @GetMapping("/config")
    @PreAuthorize("hasAuthority('SCAN_START')")
    public ResponseEntity<ApiResponse<SchedulerConfiguration>> getConfig() {
        return ResponseEntity.ok(ApiResponse.success("Scheduler configuration retrieved", schedulerService.getConfig()));
    }

    @PutMapping("/config")
    @PreAuthorize("hasAuthority('SCAN_SCHEDULE')")
    public ResponseEntity<ApiResponse<SchedulerConfiguration>> updateConfig(@RequestBody SchedulerConfigDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Scheduler configuration updated", schedulerService.updateConfig(dto)));
    }
}
