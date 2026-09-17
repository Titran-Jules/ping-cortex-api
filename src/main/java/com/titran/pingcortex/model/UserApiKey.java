package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.UUID;

public record UserApiKey(UUID id, String provider, String encryptedKey, boolean isActive, Instant createdAt) {
}
