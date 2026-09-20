package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.request.QuizSelectedOption;
import com.titran.pingcortex.dto.response.QuizAttemptResponse;
import com.titran.pingcortex.dto.response.QuizOptionAfterAttempt;
import com.titran.pingcortex.dto.response.QuizQuestionAfterAttempt;
import com.titran.pingcortex.exception.InvalidOptionException;
import com.titran.pingcortex.exception.QuizNotFoundException;
import com.titran.pingcortex.model.Difficulty;
import com.titran.pingcortex.repository.MasteryRepository;
import com.titran.pingcortex.repository.QuizAttemptRepository;
import com.titran.pingcortex.repository.QuizRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class QuizAttemptService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final MasteryRepository masteryRepository;

    private static final double K = 6.0;

    private static final Map<Difficulty, Integer> ratingFor = Map.of(
            Difficulty.EASY, 30,
            Difficulty.MEDIUM, 50,
            Difficulty.HARD, 70
    );

    @Transactional
    public QuizAttemptResponse getQuizQuestionAfterAttempt(UUID userId, UUID quizQuestionId, QuizSelectedOption quizSelectedOption) {
        QuizQuestionAfterAttempt question = quizRepository.findQuizQuestionById(userId, quizQuestionId)
                .orElseThrow(QuizNotFoundException::new);

        boolean isCorrect = question.options().stream()
                .filter(o -> o.id().equals(quizSelectedOption.selectedOptionId()))
                .findFirst()
                .map(QuizOptionAfterAttempt::isCorrect)
                .orElseThrow(InvalidOptionException::new);

        quizAttemptRepository.create(userId, quizQuestionId, quizSelectedOption.selectedOptionId(), isCorrect);

        int currentMastery = quizRepository.findOrCreateMasteryLevel(userId, question.conceptId());
        double difficultyRating = ratingFor.get(question.difficulty());
        double expected = 1.0 / (1.0 + Math.pow(10, (difficultyRating - currentMastery) / 400.0));
        double actual = isCorrect ? 1.0 : 0.0;
        int newMastery = clamp((int) Math.round(currentMastery + K * (actual - expected)), 0, 100);

        masteryRepository.updateMasteryLevel(userId, question.conceptId(), newMastery);

        UUID correctOptionId = question.options().stream()
                .filter(QuizOptionAfterAttempt::isCorrect)
                .map(QuizOptionAfterAttempt::id)
                .findFirst()
                .orElse(null);

        return new QuizAttemptResponse(isCorrect, correctOptionId, newMastery);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
