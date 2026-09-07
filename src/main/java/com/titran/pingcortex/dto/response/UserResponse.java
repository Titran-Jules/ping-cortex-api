package com.titran.pingcortex.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, String name, String level, int alertThreshold, Instant createdAt) {
}
