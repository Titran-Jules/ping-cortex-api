package com.titran.pingcortex.dto.response;

import java.util.UUID;

public record QuizAttemptResponse(boolean isCorrect, UUID correctOptionId, int newMastery) {
}
