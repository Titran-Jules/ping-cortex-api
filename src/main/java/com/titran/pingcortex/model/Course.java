package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Course(UUID id, String title, String description, Instant createdAt, List<CourseMaterial> courseMaterials, List<Concept> concepts) {
}
