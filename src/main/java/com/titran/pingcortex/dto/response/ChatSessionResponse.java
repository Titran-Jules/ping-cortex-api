package com.titran.pingcortex.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ChatSessionResponse(UUID id, UUID courseId, Instant createdAt) {
}
