package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.request.QuizSelectedOption;
import com.titran.pingcortex.dto.response.QuizAttemptResponse;
import com.titran.pingcortex.dto.response.QuizOptionAfterAttempt;
import com.titran.pingcortex.dto.response.QuizQuestionAfterAttempt;
import com.titran.pingcortex.exception.InvalidOptionException;
import com.titran.pingcortex.exception.QuizNotFoundException;
import com.titran.pingcortex.model.Difficulty;
import com.titran.pingcortex.repository.QuizAttemptRepository;
import com.titran.pingcortex.repository.QuizRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class QuizAttemptService {
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    private final static Map<Difficulty, Integer> ratingFor = Map.of(
            Difficulty.EASY, 30,
            Difficulty.MEDIUM, 50,
            Difficulty.HARD, 70
    );

    public QuizAttemptResponse getQuizQuestionAfterAttempt(UUID userId, UUID quizQuestionId, QuizSelectedOption quizSelectedOption) {
        QuizQuestionAfterAttempt quizQuestionAfterAttempt = quizRepository.findQuizQuestionById(quizQuestionId)
                .orElseThrow(QuizNotFoundException::new);

        if (quizQuestionAfterAttempt.options().stream().anyMatch(option -> option.id().equals(quizSelectedOption.selectedOptionId()))) {
            boolean isCorrect = quizQuestionAfterAttempt.options().stream()
                    .filter(o -> o.id().equals(quizSelectedOption.selectedOptionId()))
                    .findFirst()
                    .map(QuizOptionAfterAttempt::isCorrect)
                    .orElseThrow(InvalidOptionException::new);

            quizAttemptRepository.create(userId, quizQuestionId, quizSelectedOption.selectedOptionId(), isCorrect);

            int currentMastery = quizRepository.findOrCreateMasteryLevel(userId, quizQuestionAfterAttempt.conceptId());
            double difficultyRating = ratingFor.get(quizQuestionAfterAttempt.difficulty());
            double expected = 1.0 / (1.0 + Math.pow(10, (difficultyRating - currentMastery) / 400.0));
            double actual = isCorrect ? 1.0 : 0.0;
            int newMastery = (int) Math.round(currentMastery * (actual - expected));


        } else {
            throw new InvalidOptionException();
        }
    }
}
