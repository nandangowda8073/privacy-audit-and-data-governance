package com.piiscan.decision.controller;

import com.piiscan.platform.common.ApiResponse;
import com.piiscan.decision.dto.DecisionExplanationRequest;
import com.piiscan.decision.dto.ExplainableDecisionDto;
import com.piiscan.decision.service.ExplainableDecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/decisions")
@RequiredArgsConstructor
@Slf4j
public class ExplainableDecisionController {

    private final ExplainableDecisionService decisionService;

    /**
     * Explain a current or historical security/privacy decision.
     */
    @PostMapping("/explain")
    @PreAuthorize("hasAuthority('DECISION_EXPLAIN')")
    public ResponseEntity<ApiResponse<ExplainableDecisionDto>> explainDecision(
            @RequestBody DecisionExplanationRequest request) {
        log.info("API: Explaining decision of type: {}", request.getDecisionType());
        ExplainableDecisionDto result = decisionService.explainDecision(request);
        return ResponseEntity.ok(ApiResponse.success("Decision explanation generated successfully", result));
    }

    /**
     * Get a specific decision explanation by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DECISION_VIEW')")
    public ResponseEntity<ApiResponse<ExplainableDecisionDto>> getDecisionById(@PathVariable String id) {
        log.info("API: Fetching decision explanation for ID: {}", id);
        ExplainableDecisionDto result = decisionService.getDecisionById(id);
        return ResponseEntity.ok(ApiResponse.success("Decision fetched successfully", result));
    }

    /**
     * Get paginated decision history.
     */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('DECISION_VIEW')")
    public ResponseEntity<ApiResponse<Page<ExplainableDecisionDto>>> getDecisionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("API: Fetching decision history page: {}, size: {}", page, size);
        Page<ExplainableDecisionDto> history = decisionService.getDecisionHistory(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
        return ResponseEntity.ok(ApiResponse.success("Decision history retrieved successfully", history));
    }
}
