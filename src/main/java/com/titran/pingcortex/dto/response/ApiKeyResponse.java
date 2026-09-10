package com.titran.pingcortex.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(UUID id, String provider, Instant createdAt) {
}
