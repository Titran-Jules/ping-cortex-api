package com.titran.pingcortex.models;

import java.time.Instant;
import java.util.UUID;

public record QuizAttempt(UUID id, boolean isCorrect, Instant createdAt) {
}
