package com.piiscan.pii.service;

import com.piiscan.pii.dto.*;
import com.piiscan.pii.engine.PiiDetectionEngine;
import com.piiscan.pii.entity.PiiEntity;
import com.piiscan.pii.enums.PiiCategory;
import com.piiscan.pii.enums.RiskLevel;
import com.piiscan.pii.enums.DetectionMethod;
import com.piiscan.pii.exception.PiiDetectionException;
import com.piiscan.pii.repository.PiiEntityRepository;
import com.piiscan.risk.service.RiskClassificationService;
import com.piiscan.masking.service.DynamicMaskingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PiiDetectionService {

    private final PiiDetectionEngine    engine;
    private final PiiEntityRepository   piiEntityRepository;
    private final RiskClassificationService riskClassificationService;
    private final DynamicMaskingService dynamicMaskingService;
    private final com.piiscan.audit.service.AuditLogService auditLogService;

    // ── Public API ─────────────────────────────────────────────────────────────

    public DetectionResponse detectFromText(String text, String source, String detectedBy) {
        long start = System.currentTimeMillis();
        log.info("PII detection started from TEXT by '{}'", detectedBy);

        List<DetectionMatch> matches = engine.detectAll(text, source);
        List<PiiEntity> saved = persist(matches, source, null, detectedBy);

        long duration = System.currentTimeMillis() - start;
        log.info("PII detection completed. {} entities found in {}ms", saved.size(), duration);
        return buildResponse(saved, source, duration);
    }

    public DetectionResponse detectFromFile(MultipartFile file, String detectedBy) {
        if (file == null || file.isEmpty()) {
            throw new PiiDetectionException("Uploaded file is empty or missing.");
        }

        String filename = file.getOriginalFilename();
        String source = "FILE:" + filename;
        long start = System.currentTimeMillis();
        log.info("PII detection started from FILE '{}' by '{}'", filename, detectedBy);

        String text = readFileContent(file);
        List<DetectionMatch> matches = engine.detectAll(text, source);
        List<PiiEntity> saved = persist(matches, source, null, detectedBy);

        long duration = System.currentTimeMillis() - start;
        return buildResponse(saved, source, duration);
    }

    public PiiStatsDto getStats() {
        List<PiiEntity> all = piiEntityRepository.findAll();
        long total = all.size();
        long critical = piiEntityRepository.countCritical();
        long high = piiEntityRepository.countHigh();
        long medium = piiEntityRepository.countMedium();
        long low = piiEntityRepository.countLow();
        double avgConf = all.stream()
            .filter(p -> p.getConfidenceScore() != null)
            .mapToDouble(PiiEntity::getConfidenceScore)
            .average()
            .orElse(0.0);

        return PiiStatsDto.builder()
            .totalDetected(total)
            .critical(critical)
            .high(high)
            .medium(medium)
            .low(low)
            .avgConfidence(Math.round(avgConf * 100.0) / 100.0)
            .build();
    }

    public List<PiiEntityDto> getHistory() {
        List<PiiEntityDto> dtos = piiEntityRepository.findTop50ByOrderByCreatedAtDesc()
            .stream().map(this::toDto).collect(Collectors.toList());
        return dynamicMaskingService.maskAll(dtos);
    }

    // ── Internals ──────────────────────────────────────────────────────────────

    private List<PiiEntity> persist(List<DetectionMatch> matches, String source,
                                    String columnName, String detectedBy) {
        List<PiiEntity> entities = matches.stream().map(m -> PiiEntity.builder()
            .entityType(m.getEntityType())
            .category(m.getCategory())
            .originalValue(m.getOriginalValue())
            .maskedValue(m.getMaskedValue())
            .confidenceScore(m.getConfidenceScore())
            .riskLevel(m.getRiskLevel())
            .detectionMethod(m.getDetectionMethod())
            .source(source)
            .columnName(columnName)
            .startIndex(m.getStartIndex())
            .endIndex(m.getEndIndex())
            .detectedBy(detectedBy)
            .build()).collect(Collectors.toList());

        List<PiiEntity> savedEntities = piiEntityRepository.saveAll(entities);

        // Generate Risk Assessments
        savedEntities.forEach(riskClassificationService::classifyAndSave);

        // Log PII Audit Metadata (NO RAW PII VALUES EVER STORED IN AUDIT LOG)
        if (auditLogService != null && !savedEntities.isEmpty()) {
            Map<String, List<PiiEntity>> groupedByType = savedEntities.stream()
                .collect(Collectors.groupingBy(e -> e.getEntityType() != null ? e.getEntityType().name() : "UNKNOWN"));

            groupedByType.forEach((type, list) -> {
                String riskStr = list.stream()
                    .map(e -> e.getRiskLevel() != null ? e.getRiskLevel().name() : "MEDIUM")
                    .findFirst().orElse("MEDIUM");

                auditLogService.logPiiEvent(
                    com.piiscan.audit.enums.AuditAction.PII_DETECTED,
                    com.piiscan.audit.enums.AuditResult.SUCCESS,
                    "SCAN_MANUAL",
                    source,
                    columnName,
                    type,
                    riskStr,
                    list.size()
                );
            });
        }

        return savedEntities;
    }

    private String readFileContent(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(" "));
        } catch (Exception e) {
            throw new PiiDetectionException("Failed to read file content: " + e.getMessage(), e);
        }
    }

    private DetectionResponse buildResponse(List<PiiEntity> entities, String source, long durationMs) {
        Map<String, Long> byCategory = entities.stream()
            .collect(Collectors.groupingBy(e -> e.getCategory().name(), Collectors.counting()));
        Map<String, Long> byRisk = entities.stream()
            .collect(Collectors.groupingBy(e -> e.getRiskLevel().name(), Collectors.counting()));
        Map<String, Long> byMethod = entities.stream()
            .collect(Collectors.groupingBy(e -> e.getDetectionMethod().name(), Collectors.counting()));

        List<PiiEntityDto> entityDtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        List<PiiEntityDto> maskedDtos = dynamicMaskingService.maskAll(entityDtos);

        return DetectionResponse.builder()
            .totalDetected(entities.size())
            .executionTimeMs(durationMs)
            .source(source)
            .entities(maskedDtos)
            .byCategory(byCategory)
            .byRiskLevel(byRisk)
            .byDetectionMethod(byMethod)
            .build();
    }

    private PiiEntityDto toDto(PiiEntity e) {
        return PiiEntityDto.builder()
            .id(e.getId())
            .entityType(e.getEntityType())
            .category(e.getCategory())
            .originalValue(e.getOriginalValue())
            .maskedValue(e.getMaskedValue())
            .confidenceScore(e.getConfidenceScore())
            .riskLevel(e.getRiskLevel())
            .detectionMethod(e.getDetectionMethod())
            .source(e.getSource())
            .columnName(e.getColumnName())
            .startIndex(e.getStartIndex())
            .endIndex(e.getEndIndex())
            .detectedBy(e.getDetectedBy())
            .createdAt(e.getCreatedAt())
            .build();
    }
}
