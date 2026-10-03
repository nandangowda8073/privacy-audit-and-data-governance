package com.piiscan.pii.controller;

import com.piiscan.pii.dto.*;
import com.piiscan.pii.enums.PiiCategory;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.service.PiiDetectionService;
import com.piiscan.platform.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pii")
@RequiredArgsConstructor
public class PiiDetectionController {

    private final PiiDetectionService detectionService;

    /** Detect PII from raw text */
    @PostMapping("/detect")
    @PreAuthorize("hasAuthority('SCAN_START')")
    public ResponseEntity<ApiResponse<DetectionResponse>> detectFromText(
            @RequestBody DetectionRequest request,
            Authentication auth) {

        DetectionResponse response = detectionService.detectFromText(
            request.getText(),
            request.getSource() != null ? request.getSource() : "TEXT_INPUT",
            auth.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("PII detection completed", response));
    }

    /** Detect PII from an uploaded file */
    @PostMapping("/file")
    @PreAuthorize("hasAuthority('SCAN_START')")
    public ResponseEntity<ApiResponse<DetectionResponse>> detectFromFile(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        DetectionResponse response = detectionService.detectFromFile(file, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("File PII detection completed", response));
    }

    /** Return aggregated stats / KPI data */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('PII_VIEW')")
    public ResponseEntity<ApiResponse<PiiStatsDto>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("PII statistics", detectionService.getStats()));
    }

    /** Return last 50 detected entities */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('PII_VIEW')")
    public ResponseEntity<ApiResponse<List<PiiEntityDto>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.success("PII detection history", detectionService.getHistory()));
    }

    /** Return all supported PII entity types */
    @GetMapping("/types")
    @PreAuthorize("hasAuthority('PII_VIEW')")
    public ResponseEntity<ApiResponse<List<String>>> getTypes() {
        List<String> types = Arrays.stream(PiiEntityType.values())
            .map(Enum::name).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Supported PII types", types));
    }

    /** Return all supported PII categories */
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PII_VIEW')")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> cats = Arrays.stream(PiiCategory.values())
            .map(Enum::name).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Supported PII categories", cats));
    }
}
