package com.titran.pingcortex.models;

import java.util.UUID;

public record QuizOption(UUID id, String text, boolean isCorrect) {
}
