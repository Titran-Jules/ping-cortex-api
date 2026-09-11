package com.titran.pingcortex.dto.response;

import com.titran.pingcortex.model.AnalysisStatus;

import java.time.Instant;
import java.util.UUID;

public record CourseResponse(UUID id, String title, String description, boolean isActive, AnalysisStatus analysisStatus, Instant createdAt) {
}
