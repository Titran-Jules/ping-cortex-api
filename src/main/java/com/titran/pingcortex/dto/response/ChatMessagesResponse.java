package com.titran.pingcortex.dto.response;

import com.titran.pingcortex.model.Role;

import java.time.Instant;
import java.util.UUID;

public record ChatMessagesResponse(UUID id, Role role, String content, Instant createdAt) {
}
