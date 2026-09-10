package com.titran.pingcortex.dto.request;

import java.util.UUID;

public record AccessTokenClaims(UUID id, String email, String level, int tokenVersion) {
}
