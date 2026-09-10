package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.UUID;

public record User(UUID id, String email, String passwordHash, String name, String level, int tokenVersion, Integer alertThreshold, Instant createdAt) {}
