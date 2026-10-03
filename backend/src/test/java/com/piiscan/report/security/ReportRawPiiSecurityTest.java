package com.piiscan.report.security;

import com.piiscan.report.dto.ReportDto;
import com.piiscan.report.dto.ReportGenerateRequest;
import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.enums.ReportType;
import com.piiscan.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReportRawPiiSecurityTest {

    @Autowired
    private ReportService reportService;

    private static final String[] SENSITIVE_PROHIBITED_STRINGS = {
        "456789123456",        // Aadhaar
        "ABCDE1234F",          // PAN
        "4532123412345678",    // Credit Card
        "db_password_secret",  // DB Credential
        "eyJhbGciOiJIUzI1NiJ9" // JWT token prefix
    };

    @Test
    @DisplayName("Verify Zero Raw PII in PDF and CSV Reports across all report types")
    void testZeroRawPiiInReports() {
        for (ReportType type : ReportType.values()) {
            for (ReportFormat format : ReportFormat.values()) {
                ReportGenerateRequest req = ReportGenerateRequest.builder()
                        .reportType(type)
                        .format(format)
                        .build();

                ReportDto report = reportService.generateReport(req, "sec_auditor");
                byte[] content = reportService.downloadReportFile(report.getId());
                String reportText = new String(content, StandardCharsets.UTF_8);

                for (String prohibited : SENSITIVE_PROHIBITED_STRINGS) {
                    assertFalse(
                        reportText.contains(prohibited),
                        String.format("CRITICAL SECURITY VIOLATION: Report type %s (%s) contained raw sensitive string '%s'", type, format, prohibited)
                    );
                }
            }
        }
    }
}
