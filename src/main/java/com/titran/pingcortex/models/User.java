package com.titran.pingcortex.models;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record User(UUID id, String email, String password, Instant createdAt, List<Course> courses, List<UserConceptMastery> userConceptMasteries, List<ChatSession> chatSessions) {}
