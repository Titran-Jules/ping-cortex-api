package com.titran.pingcortex.dto.response;

import com.titran.pingcortex.model.Difficulty;

import java.util.List;
import java.util.UUID;

public record QuizQuestionAfterAttempt(UUID id, UUID conceptId, String questionText, Difficulty difficulty, List<QuizOptionAfterAttempt> options) {
}
