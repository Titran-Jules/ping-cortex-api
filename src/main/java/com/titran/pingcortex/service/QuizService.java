package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.response.QuizOptionResponse;
import com.titran.pingcortex.dto.response.QuizQuestionResponse;
import com.titran.pingcortex.model.Difficulty;
import com.titran.pingcortex.repository.QuizRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;

    private static final Map<String, Map<Difficulty, Integer>> COUNT_BY_MASTERY = Map.of(
            "LOW", Map.of(Difficulty.EASY, 6, Difficulty.MEDIUM, 3, Difficulty.HARD, 1),
            "MEDIUM", Map.of(Difficulty.EASY, 3, Difficulty.MEDIUM, 4, Difficulty.HARD, 3),
            "HIGH", Map.of(Difficulty.EASY, 1, Difficulty.MEDIUM, 3, Difficulty.HARD, 6)
    );

    public List<QuizQuestionResponse> selectQuizQuestions(UUID userId, UUID conceptId) {
        int masteryLevel = quizRepository.findOrCreateMasteryLevel(userId, conceptId);
        String masteryBucket;
        if (masteryLevel < 30) {
            masteryBucket = "LOW";
        } else if (masteryLevel < 70) {
            masteryBucket = "MEDIUM";
        } else {
            masteryBucket = "HIGH";
        }
        Map<Difficulty, Integer> countByDifficulty = COUNT_BY_MASTERY.get(masteryBucket);
        List<QuizQuestionResponse> quizQuestions = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            quizQuestions.addAll(
                    quizRepository.selectQuestionsForQuiz(userId, conceptId, difficulty, countByDifficulty.get(difficulty)));
        }
        List<UUID> questionIds = quizQuestions.stream().map(QuizQuestionResponse::id).toList();
        Map<UUID, List<QuizOptionResponse>> optionsByQuestion = quizRepository.findQuizOptionsByQuestionIds(questionIds);
        for (QuizQuestionResponse question : quizQuestions) {
            question.options().addAll(optionsByQuestion.getOrDefault(question.id(), List.of()));
        }
        return quizQuestions;
    }
}