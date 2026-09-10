package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatSession(UUID id, Instant createdAt, List<ChatMessage> chatMessages) {
}
