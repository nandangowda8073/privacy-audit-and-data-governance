package com.piiscan.report.service;

import com.piiscan.report.dto.ReportDto;
import com.piiscan.report.dto.ReportGenerateRequest;
import com.piiscan.report.dto.ReportPreviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    ReportDto generateReport(ReportGenerateRequest request, String currentUsername);
    ReportPreviewResponse previewReport(ReportGenerateRequest request);
    byte[] downloadReportFile(Long reportId);
    Page<ReportDto> getReportHistory(Pageable pageable);
    ReportDto getReportById(Long id);
}
