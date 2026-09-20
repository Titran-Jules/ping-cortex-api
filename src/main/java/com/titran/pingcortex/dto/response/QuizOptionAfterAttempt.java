package com.titran.pingcortex.dto.response;

import java.util.UUID;

public record QuizOptionAfterAttempt(UUID id, String text, boolean isCorrect) {
}
