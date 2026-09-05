package com.titran.pingcortex.models;

import java.time.Instant;
import java.util.UUID;

public record FeynmanSubmission(UUID id, String explanationText, String aiEvaluation, float score, Instant createdAt) {
}
