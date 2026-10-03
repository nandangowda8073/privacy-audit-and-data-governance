package com.piiscan.report.service.impl;

import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import com.piiscan.audit.service.AuditLogService;
import com.piiscan.report.dto.ReportDataContainer;
import com.piiscan.report.dto.ReportDto;
import com.piiscan.report.dto.ReportGenerateRequest;
import com.piiscan.report.dto.ReportPreviewResponse;
import com.piiscan.report.entity.Report;
import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.enums.ReportStatus;
import com.piiscan.report.enums.ReportType;
import com.piiscan.report.generator.CsvReportGenerator;
import com.piiscan.report.generator.PdfReportGenerator;
import com.piiscan.report.repository.ReportRepository;
import com.piiscan.report.service.ReportDataService;
import com.piiscan.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportDataService reportDataService;
    private final PdfReportGenerator pdfReportGenerator;
    private final CsvReportGenerator csvReportGenerator;
    private final AuditLogService auditLogService;

    private static final String STORAGE_DIR = "./storage/reports";

    @Override
    @Transactional
    public ReportDto generateReport(ReportGenerateRequest request, String currentUsername) {
        ReportType type = request.getReportType() != null ? request.getReportType() : ReportType.SCAN_SUMMARY;
        ReportFormat format = request.getFormat() != null ? request.getFormat() : ReportFormat.PDF;
        String username = currentUsername != null ? currentUsername : "SYSTEM";

        request.setUsername(username);

        String ext = format == ReportFormat.PDF ? ".pdf" : ".csv";
        String timeStampStr = String.valueOf(System.currentTimeMillis());
        String fileName = type.name().toLowerCase().replace("_", "-") + "-" + timeStampStr + ext;

        // 1. Initial Pending Record
        Report report = Report.builder()
                .reportType(type)
                .format(format)
                .generatedBy(username)
                .generatedAt(LocalDateTime.now())
                .dateFrom(request.getDateFrom() != null ? request.getDateFrom() : LocalDate.now().minusDays(30))
                .dateTo(request.getDateTo() != null ? request.getDateTo() : LocalDate.now())
                .databaseId(request.getDatabaseId())
                .scanId(request.getScanId())
                .status(ReportStatus.GENERATING)
                .fileName(fileName)
                .description("Generating " + type.name() + " report in " + format.name() + " format.")
                .build();

        report = reportRepository.save(report);

        try {
            // 2. Collect Data & Generate Content
            ReportDataContainer data = reportDataService.collectReportData(request);

            byte[] content;
            if (format == ReportFormat.PDF) {
                content = pdfReportGenerator.generatePdf(data);
            } else {
                content = csvReportGenerator.generateCsv(data);
            }

            // 3. Save File to Disk Storage
            File dir = new File(STORAGE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Path filePath = Paths.get(STORAGE_DIR, fileName);
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(content);
            }

            // 4. Update Report Entity
            report.setStatus(ReportStatus.COMPLETED);
            report.setFilePath(filePath.toAbsolutePath().toString());
            report.setFileSize((long) content.length);
            report = reportRepository.save(report);

            // 5. Audit Report Generation Event (No recursion)
            if (auditLogService != null) {
                auditLogService.logReportEvent(
                        AuditAction.REPORT_GENERATED,
                        AuditResult.SUCCESS,
                        type.name(),
                        String.valueOf(report.getId()),
                        "Generated " + type.name() + " report (#" + report.getId() + ") in " + format.name() + " format."
                );
            }

            return toDto(report);

        } catch (Exception e) {
            log.error("Report generation failed for ID: {}", report.getId(), e);
            report.setStatus(ReportStatus.FAILED);
            report.setDescription("Generation failed: " + e.getMessage());
            reportRepository.save(report);

            if (auditLogService != null) {
                auditLogService.logReportEvent(
                        AuditAction.REPORT_GENERATED,
                        AuditResult.FAILURE,
                        type.name(),
                        String.valueOf(report.getId()),
                        "Report generation failed: " + e.getMessage()
                );
            }
            throw new RuntimeException("Report Generation Failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportPreviewResponse previewReport(ReportGenerateRequest request) {
        ReportType type = request.getReportType() != null ? request.getReportType() : ReportType.SCAN_SUMMARY;
        ReportFormat format = request.getFormat() != null ? request.getFormat() : ReportFormat.PDF;
        ReportDataContainer data = reportDataService.collectReportData(request);

        return ReportPreviewResponse.builder()
                .reportType(type)
                .format(format)
                .title(data.getReportTitle())
                .dateFrom(data.getDateFrom())
                .dateTo(data.getDateTo())
                .totalRecordsScanned(data.getTotalRecordsScanned())
                .totalFindings(data.getTotalPiiFindings())
                .riskDistribution(data.getRiskDistribution())
                .estimatedSize(format == ReportFormat.PDF ? "145 KB" : "18 KB")
                .description("Preview metadata calculated for " + type.name() + " (" + format.name() + "). Metadata only.")
                .build();
    }

    @Override
    public byte[] downloadReportFile(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        if (report.getStatus() != ReportStatus.COMPLETED) {
            throw new RuntimeException("Report is not ready for download. Current status: " + report.getStatus());
        }

        try {
            byte[] bytes;
            if (report.getFilePath() != null && Files.exists(Paths.get(report.getFilePath()))) {
                bytes = Files.readAllBytes(Paths.get(report.getFilePath()));
            } else {
                // Fallback on-demand generation if file was purged from temp storage
                ReportGenerateRequest req = ReportGenerateRequest.builder()
                        .reportType(report.getReportType())
                        .format(report.getFormat())
                        .dateFrom(report.getDateFrom())
                        .dateTo(report.getDateTo())
                        .databaseId(report.getDatabaseId())
                        .scanId(report.getScanId())
                        .username(report.getGeneratedBy())
                        .build();
                ReportDataContainer data = reportDataService.collectReportData(req);
                bytes = report.getFormat() == ReportFormat.PDF ?
                        pdfReportGenerator.generatePdf(data) : csvReportGenerator.generateCsv(data);
            }

            // Audit Download Event
            if (auditLogService != null) {
                auditLogService.logReportEvent(
                        AuditAction.REPORT_DOWNLOADED,
                        AuditResult.SUCCESS,
                        report.getReportType().name(),
                        String.valueOf(report.getId()),
                        "Downloaded report file: " + report.getFileName()
                );
            }

            return bytes;

        } catch (Exception e) {
            log.error("Failed to download report ID: {}", reportId, e);
            throw new RuntimeException("Could not read report file: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ReportDto> getReportHistory(Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public ReportDto getReportById(Long id) {
        return reportRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));
    }

    private ReportDto toDto(Report r) {
        return ReportDto.builder()
                .id(r.getId())
                .reportType(r.getReportType())
                .format(r.getFormat())
                .generatedBy(r.getGeneratedBy())
                .generatedAt(r.getGeneratedAt())
                .dateFrom(r.getDateFrom())
                .dateTo(r.getDateTo())
                .databaseId(r.getDatabaseId())
                .scanId(r.getScanId())
                .status(r.getStatus())
                .fileName(r.getFileName())
                .fileSize(r.getFileSize())
                .description(r.getDescription())
                .build();
    }
}
