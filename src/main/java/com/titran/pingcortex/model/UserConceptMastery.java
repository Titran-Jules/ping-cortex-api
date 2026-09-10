package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.UUID;

public record UserConceptMastery(UUID id, int masteryLevel, Instant lastReviewedAt) {
}
