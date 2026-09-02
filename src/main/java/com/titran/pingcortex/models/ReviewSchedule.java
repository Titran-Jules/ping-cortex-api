package com.titran.pingcortex.models;

import java.time.Instant;
import java.util.UUID;

public record ReviewSchedule(UUID id, Instant nextReviewAt, int intervalDays) {
}
