package com.titran.pingcortex.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewTodayResponse(UUID conceptId, String conceptName, UUID courseId, String courseTitle, Instant nextReviewAt, int intervalDays) {
}
