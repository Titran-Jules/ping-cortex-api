package com.titran.pingcortex.dto.response;

import com.titran.pingcortex.ai.AiResults;

import java.time.Instant;
import java.util.UUID;

public record FeynmanSubmissionResponse(UUID id, String explanationText, AiResults.FeynmanEvaluationResult aiEvaluation, double score, Instant createdAt) {
}
