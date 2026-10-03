package com.piiscan.dashboard.controller;

import com.piiscan.dashboard.dto.ActivityLogDto;
import com.piiscan.dashboard.dto.DashboardChartsDto;
import com.piiscan.dashboard.dto.DashboardOverviewDto;
import com.piiscan.dashboard.dto.ScanResultDto;
import com.piiscan.dashboard.service.DashboardService;
import com.piiscan.platform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> getOverview(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        DashboardOverviewDto overview = dashboardService.getOverview(range, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success("Dashboard overview retrieved", overview));
    }

    @GetMapping("/activity")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<List<ActivityLogDto>>> getRecentActivities(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        List<ActivityLogDto> activities = dashboardService.getRecentActivities(range, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success("Recent activities retrieved", activities));
    }

    @GetMapping("/charts")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<DashboardChartsDto>> getChartsData(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        DashboardChartsDto chartsData = dashboardService.getChartsData(range, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success("Dashboard charts data retrieved", chartsData));
    }

    @GetMapping("/scans")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<List<ScanResultDto>>> getRecentScans(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        List<ScanResultDto> scans = dashboardService.getRecentScans(range, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success("Recent scans retrieved", scans));
    }
}
