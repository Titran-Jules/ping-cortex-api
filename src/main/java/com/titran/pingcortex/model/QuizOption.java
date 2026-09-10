package com.titran.pingcortex.model;

import java.util.UUID;

public record QuizOption(UUID id, String text, boolean isCorrect) {
}
