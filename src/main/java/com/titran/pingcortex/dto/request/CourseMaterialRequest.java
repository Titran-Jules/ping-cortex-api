package com.titran.pingcortex.dto.request;

import com.titran.pingcortex.model.MaterialType;

public record CourseMaterialRequest(String content, MaterialType type) {
}
