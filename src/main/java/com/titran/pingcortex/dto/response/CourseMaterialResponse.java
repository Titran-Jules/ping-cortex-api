package com.titran.pingcortex.dto.response;

import com.titran.pingcortex.model.MaterialType;

import java.time.Instant;
import java.util.UUID;

public record CourseMaterialResponse(UUID id, String content, MaterialType type, Instant createdAt) {
}
