package com.titran.pingcortex.ai;

import com.titran.pingcortex.model.Difficulty;

import java.util.List;
import java.util.UUID;

public final class AiResults {
    private AiResults() {}

    public record ConceptSuggestion(String name, String description) {}

    public record ConceptCoverageSuggestion(UUID conceptId, String confidence) {}

    public record ConceptSummary(UUID id, String name, String description) {
    }

    public record QuizQuestionSuggestion(
            String questionText,
            Difficulty difficulty,
            List<QuizOptionSuggestion> options
    ) {}

    public record QuizOptionSuggestion(String text, boolean correct) {}

    public record FeynmanEvaluationResult(
        List<CriterionResult> criteria,
        String overallFeedback,
        List<String> misconceptions,
        double score
    ) {}

    public record CriterionResult(String name, String comment) {}

    public record ChatTurn(String role, String content) {}
}
