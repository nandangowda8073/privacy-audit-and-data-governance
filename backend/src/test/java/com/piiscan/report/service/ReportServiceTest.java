package com.piiscan.report.service;

import com.piiscan.report.dto.ReportDto;
import com.piiscan.report.dto.ReportGenerateRequest;
import com.piiscan.report.dto.ReportPreviewResponse;
import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.enums.ReportStatus;
import com.piiscan.report.enums.ReportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Test
    @DisplayName("Generate Scan Summary PDF & CSV Reports")
    void testScanSummaryReports() {
        testReportGeneration(ReportType.SCAN_SUMMARY, ReportFormat.PDF);
        testReportGeneration(ReportType.SCAN_SUMMARY, ReportFormat.CSV);
    }

    @Test
    @DisplayName("Generate PII Detection PDF & CSV Reports")
    void testPiiDetectionReports() {
        testReportGeneration(ReportType.PII_DETECTION, ReportFormat.PDF);
        testReportGeneration(ReportType.PII_DETECTION, ReportFormat.CSV);
    }

    @Test
    @DisplayName("Generate Risk Assessment PDF & CSV Reports")
    void testRiskAssessmentReports() {
        testReportGeneration(ReportType.RISK_ASSESSMENT, ReportFormat.PDF);
        testReportGeneration(ReportType.RISK_ASSESSMENT, ReportFormat.CSV);
    }

    @Test
    @DisplayName("Generate Compliance PDF & CSV Reports")
    void testComplianceReports() {
        testReportGeneration(ReportType.COMPLIANCE, ReportFormat.PDF);
        testReportGeneration(ReportType.COMPLIANCE, ReportFormat.CSV);
    }

    @Test
    @DisplayName("Generate Audit Log PDF & CSV Reports")
    void testAuditLogReports() {
        testReportGeneration(ReportType.AUDIT_LOG, ReportFormat.PDF);
        testReportGeneration(ReportType.AUDIT_LOG, ReportFormat.CSV);
    }

    @Test
    @DisplayName("Generate Database Risk PDF & CSV Reports")
    void testDatabaseRiskReports() {
        testReportGeneration(ReportType.DATABASE_RISK, ReportFormat.PDF);
        testReportGeneration(ReportType.DATABASE_RISK, ReportFormat.CSV);
    }

    @Test
    @DisplayName("Preview Report Metadata")
    void testPreviewReport() {
        ReportGenerateRequest req = ReportGenerateRequest.builder()
                .reportType(ReportType.RISK_ASSESSMENT)
                .format(ReportFormat.PDF)
                .dateFrom(LocalDate.now().minusDays(7))
                .dateTo(LocalDate.now())
                .build();

        ReportPreviewResponse preview = reportService.previewReport(req);
        assertNotNull(preview);
        assertEquals(ReportType.RISK_ASSESSMENT, preview.getReportType());
        assertTrue(preview.getTotalFindings() > 0);
    }

    private void testReportGeneration(ReportType type, ReportFormat format) {
        ReportGenerateRequest req = ReportGenerateRequest.builder()
                .reportType(type)
                .format(format)
                .dateFrom(LocalDate.now().minusDays(14))
                .dateTo(LocalDate.now())
                .build();

        ReportDto result = reportService.generateReport(req, "test_admin");
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(ReportStatus.COMPLETED, result.getStatus());

        byte[] downloaded = reportService.downloadReportFile(result.getId());
        assertNotNull(downloaded);
        assertTrue(downloaded.length > 0);
    }
}
