package com.piiscan.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardChartsDto {
    private List<CategoryCount> piiByCategory;
    private List<RiskCount> riskDistribution;
    private List<DailyTrend> dailyScanTrend;
    private List<DbScanStatus> databaseScanStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryCount {
        private String category; // "Aadhaar", "PAN", "Email", "Phone", "Bank Account"
        private long count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskCount {
        private String level; // "Low", "Medium", "High", "Critical"
        private long count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend {
        private String date;
        private long scans;
        private long piiFound;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DbScanStatus {
        private String database;
        private int scannedPercentage;
        private long piiCount;
        private String status;
    }
}
