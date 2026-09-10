package com.titran.pingcortex.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public final class CurrentUser {
    private CurrentUser() {}

    public static UUID id(HttpServletRequest request) {
        Object userId = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        if (userId == null) {
            throw new IllegalStateException("No authenticated user on this request");
        }
        return (UUID) userId;
    }
}
