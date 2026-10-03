package com.piiscan.dashboard.service;

import com.piiscan.dashboard.dto.ActivityLogDto;
import com.piiscan.dashboard.dto.DashboardChartsDto;
import com.piiscan.dashboard.dto.DashboardOverviewDto;
import com.piiscan.dashboard.dto.ScanResultDto;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardOverviewDto getOverview(String range, LocalDate dateFrom, LocalDate dateTo);
    List<ActivityLogDto> getRecentActivities(String range, LocalDate dateFrom, LocalDate dateTo);
    DashboardChartsDto getChartsData(String range, LocalDate dateFrom, LocalDate dateTo);
    List<ScanResultDto> getRecentScans(String range, LocalDate dateFrom, LocalDate dateTo);
}
