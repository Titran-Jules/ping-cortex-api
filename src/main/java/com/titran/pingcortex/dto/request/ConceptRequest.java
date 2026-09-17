package com.titran.pingcortex.dto.request;

import java.util.UUID;

public record ConceptRequest(UUID courseId, String name, String description, int position) {
}
