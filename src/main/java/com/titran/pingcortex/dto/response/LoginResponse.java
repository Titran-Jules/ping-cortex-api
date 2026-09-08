package com.titran.pingcortex.dto.response;

public record LoginResponse(String accessToken, String refreshToken, JwtPayload jwtPayload) {
}
