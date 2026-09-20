package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.request.QuizSelectedOption;
import com.titran.pingcortex.dto.response.QuizOptionAfterAttempt;
import com.titran.pingcortex.dto.response.QuizQuestionAfterAttempt;
import com.titran.pingcortex.exception.InvalidOptionException;
import com.titran.pingcortex.exception.QuizNotFoundException;
import com.titran.pingcortex.repository.QuizRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class QuizAttemptService {
    private final QuizRepository quizRepository;

    public QuizQuestionAfterAttempt getQuizQuestionAfterAttempt(UUID userId, UUID quizQuestionId, QuizSelectedOption quizSelectedOption) {
        QuizQuestionAfterAttempt quizQuestionAfterAttempt = quizRepository.findQuizQuestionById(quizQuestionId)
                .orElseThrow(QuizNotFoundException::new);

        if (quizQuestionAfterAttempt.options().stream().anyMatch(option -> option.id().equals(quizSelectedOption.selectedOptionId()))) {
            boolean isCorrect = quizQuestionAfterAttempt.options().stream()
                    .filter(o -> o.id().equals(quizSelectedOption.selectedOptionId()))
                    .findFirst()
                    .map(QuizOptionAfterAttempt::isCorrect)
                    .orElseThrow(InvalidOptionException::new);


        } else {
            throw new InvalidOptionException();
        }
    }
}
