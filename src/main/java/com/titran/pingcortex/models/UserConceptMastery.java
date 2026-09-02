package com.titran.pingcortex.models;

import java.time.Instant;
import java.util.UUID;

public record UserConceptMastery(UUID id, int masteryLevel, Instant lastReviewedAt) {
}
