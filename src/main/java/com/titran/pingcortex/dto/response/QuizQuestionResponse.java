package com.titran.pingcortex.dto.response;

import com.titran.pingcortex.model.Difficulty;

import java.util.List;
import java.util.UUID;

public record QuizQuestionResponse(UUID id, String questionText, Difficulty difficulty, List<QuizOptionResponse> options) {
}
