package com.piiscan.report.controller;

import com.piiscan.platform.common.ApiResponse;
import com.piiscan.report.dto.ReportDto;
import com.piiscan.report.dto.ReportGenerateRequest;
import com.piiscan.report.dto.ReportPreviewResponse;
import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<Page<ReportDto>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "generatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReportDto> reports = reportService.getReportHistory(pageable);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", reports));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<ReportDto>> getReportById(@PathVariable Long id) {
        ReportDto report = reportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success("Report retrieved", report));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<ApiResponse<ReportDto>> generateReport(@RequestBody ReportGenerateRequest request) {
        String username = currentUsername();
        ReportDto report = reportService.generateReport(request, username);
        return ResponseEntity.ok(ApiResponse.success("Report generated successfully", report));
    }

    @PostMapping("/preview")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<ReportPreviewResponse>> previewReport(@RequestBody ReportGenerateRequest request) {
        ReportPreviewResponse preview = reportService.previewReport(request);
        return ResponseEntity.ok(ApiResponse.success("Report preview generated", preview));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('REPORT_DOWNLOAD')")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        ReportDto report = reportService.getReportById(id);
        byte[] fileData = reportService.downloadReportFile(id);

        MediaType mediaType = report.getFormat() == ReportFormat.PDF ?
                MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFileName() + "\"")
                .body(fileData);
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }
}
