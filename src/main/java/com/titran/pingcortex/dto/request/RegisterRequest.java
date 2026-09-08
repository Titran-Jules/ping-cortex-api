package com.titran.pingcortex.dto.request;

public record RegisterRequest(String email, String name, String passwordHash, String level) {
}
