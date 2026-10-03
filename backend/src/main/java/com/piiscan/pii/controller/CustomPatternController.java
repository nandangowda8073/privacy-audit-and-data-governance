package com.piiscan.pii.controller;

import com.piiscan.pii.dto.CustomPatternDto;
import com.piiscan.pii.entity.CustomPattern;
import com.piiscan.pii.service.CustomPatternService;
import com.piiscan.platform.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pii/custom-pattern")
@RequiredArgsConstructor
public class CustomPatternController {

    private final CustomPatternService customPatternService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCAN_VIEW')")
    public ResponseEntity<ApiResponse<List<CustomPattern>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Custom patterns", customPatternService.getAll()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCAN_START')")
    public ResponseEntity<ApiResponse<CustomPattern>> create(
            @RequestBody CustomPatternDto dto,
            Authentication auth) {
        CustomPattern created = customPatternService.create(dto, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Custom pattern created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCAN_START')")
    public ResponseEntity<ApiResponse<CustomPattern>> update(
            @PathVariable Long id,
            @RequestBody CustomPatternDto dto) {
        CustomPattern updated = customPatternService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Custom pattern updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCAN_START')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customPatternService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Custom pattern deleted", null));
    }
}
