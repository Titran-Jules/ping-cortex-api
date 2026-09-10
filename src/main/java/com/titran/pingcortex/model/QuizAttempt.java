package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.UUID;

public record QuizAttempt(UUID id, boolean isCorrect, Instant createdAt) {
}
