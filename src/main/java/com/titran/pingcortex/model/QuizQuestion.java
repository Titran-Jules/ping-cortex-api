package com.titran.pingcortex.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizQuestion(UUID id, String questionText, Difficulty difficulty, Instant createdAt, List<QuizOption> quizOptions, QuizAttempt quizAttempt) {
}
