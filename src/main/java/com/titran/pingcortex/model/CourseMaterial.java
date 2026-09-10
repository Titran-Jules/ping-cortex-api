package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.UUID;

public record CourseMaterial(UUID id, String content, CourseMaterialType type, Instant createdAt) {
}
