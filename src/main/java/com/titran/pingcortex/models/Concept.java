package com.titran.pingcortex.models;

import java.util.List;
import java.util.UUID;

public record Concept(UUID id, String name, String description, int order, List<QuizQuestion> quizQuestions, List<FeynmanSubmission> feynmanSubmissions, List<ReviewSchedule> reviewSchedules) {
}
