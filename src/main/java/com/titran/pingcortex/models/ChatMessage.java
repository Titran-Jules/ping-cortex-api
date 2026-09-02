package com.titran.pingcortex.models;

import java.time.Instant;
import java.util.UUID;

public record ChatMessage(UUID id, Role role, String content, Instant createdAt) {
}
