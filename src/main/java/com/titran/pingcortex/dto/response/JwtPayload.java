package com.titran.pingcortex.dto.response;

import java.util.UUID;

public record JwtPayload(UUID id, String email, String level) {
}
