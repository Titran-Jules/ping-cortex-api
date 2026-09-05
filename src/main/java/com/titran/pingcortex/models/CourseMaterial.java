package com.titran.pingcortex.models;

import java.time.Instant;
import java.util.UUID;

public record CourseMaterial(UUID id, String content, CourseMaterialType type, Instant createdAt) {
}
