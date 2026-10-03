package com.piiscan.decision.service;

import com.piiscan.decision.dto.DecisionExplanationRequest;
import com.piiscan.decision.dto.ExplainableDecisionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExplainableDecisionService {
    ExplainableDecisionDto explainDecision(DecisionExplanationRequest request);
    ExplainableDecisionDto getDecisionById(String decisionId);
    Page<ExplainableDecisionDto> getDecisionHistory(Pageable pageable);
}
