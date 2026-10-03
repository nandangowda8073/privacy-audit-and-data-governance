package com.piiscan.report.service;

import com.piiscan.report.dto.ReportDataContainer;
import com.piiscan.report.dto.ReportGenerateRequest;

public interface ReportDataService {
    ReportDataContainer collectReportData(ReportGenerateRequest request);
}
