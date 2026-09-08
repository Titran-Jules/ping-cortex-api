package com.titran.pingcortex.dto.request;

import java.util.UUID;

public record JwtClaims(UUID id, String email, String level, int tokenVersion) {
}
