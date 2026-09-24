package com.titran.pingcortex.service;

import com.titran.pingcortex.model.SuggestedActivity;
import com.titran.pingcortex.repository.FeynmanRepository;
import com.titran.pingcortex.repository.QuizAttemptRepository;
import com.titran.pingcortex.repository.QuizRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewActivityService {
    private final QuizRepository quizRepository;
    private final FeynmanRepository feynmanRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public SuggestedActivity nextActivity(UUID userId, UUID conceptId) {
        int mastery = quizRepository.findOrCreateMasteryLevel(userId, conceptId);
        Optional<Instant> lastFeynmanAt = feynmanRepository.findLastSubmissionDate(userId, conceptId);

        if (lastFeynmanAt.isEmpty() && mastery >= 50) return SuggestedActivity.FEYNMAN;
        if (mastery >= 70 && mastery < 80 && lastFeynmanAt.isEmpty()) return SuggestedActivity.FEYNMAN;

        int attemptsSinceLastFeynman = quizAttemptRepository.countSince(userId, conceptId, lastFeynmanAt.orElse(Instant.EPOCH));
        if (attemptsSinceLastFeynman >= 25) return SuggestedActivity.FEYNMAN;

        return SuggestedActivity.QUIZ;
    }
}
