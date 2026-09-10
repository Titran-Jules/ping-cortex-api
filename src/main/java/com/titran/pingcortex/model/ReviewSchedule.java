package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.UUID;

public record ReviewSchedule(UUID id, Instant nextReviewAt, int intervalDays) {
}
