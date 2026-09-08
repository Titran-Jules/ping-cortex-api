package com.titran.pingcortex.models;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record User(UUID id, String email, String password, String name, String level, int tokenVersion, Integer alertThreshold, Instant createdAt) {}
