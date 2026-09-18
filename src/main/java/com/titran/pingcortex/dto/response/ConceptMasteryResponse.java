package com.titran.pingcortex.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConceptMasteryResponse(UUID conceptId, int masteryLevel, Instant lastReviewedAt) {
}
