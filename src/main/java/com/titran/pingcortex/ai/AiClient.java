package com.titran.pingcortex.ai;

import com.titran.pingcortex.ai.AiResults.*;
import com.titran.pingcortex.model.Difficulty;

import java.util.List;

public interface AiClient {
    List<ConceptSuggestion> analyzeCourse(
            String apiKey,
            String courseTitle,
            String courseDescription,
            List<String> materialContents
    );

    QuizQuestionSuggestion generateQuiz(
            String apiKey,
            String conceptName,
            String conceptDescription,
            Difficulty difficulty
    );

    FeynmanEvaluationResult evaluateFeynman(
            String apiKey,
            String conceptName,
            String conceptDescription,
            String explanationText
    );

    String chat(
            String apiKey,
            String courseContext,
            List<ChatTurn> history,
            String newMessage
    );
}
