package com.titran.pingcortex.service;

import com.titran.pingcortex.ai.AiResults;
import com.titran.pingcortex.dto.response.FeynmanSubmissionResponse;
import com.titran.pingcortex.repository.FeynmanRepository;
import com.titran.pingcortex.repository.MasteryRepository;
import com.titran.pingcortex.repository.QuizRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class FeynmanPersistenceService {
    private final FeynmanRepository feynmanRepository;
    private final MasteryRepository masteryRepository;
    private final ReviewScheduleService reviewScheduleService;
    private final QuizRepository quizRepository;

    @Transactional
    public FeynmanSubmissionResponse persistSubmission(UUID userId, UUID conceptId, String explanationText, AiResults.FeynmanEvaluationResult result) {
        int newMastery = calculateNewMastery(userId, conceptId, result.score());
        masteryRepository.updateMasteryLevel(userId, conceptId, newMastery);

        int quality = clamp((int) Math.round(result.score() / 100.0 * 5), 0, 5);
        reviewScheduleService.applyReviewIfDue(userId, conceptId, quality);

        return feynmanRepository.create(userId, conceptId, explanationText, result, result.score());
    }

    private int calculateNewMastery(UUID userId, UUID conceptId, double score) {
        int currentMastery = quizRepository.findOrCreateMasteryLevel(userId, conceptId);
        double actual = score / 100.0;
        double expected = 1.0 / (1.0 + Math.pow(10.0, (60 - currentMastery) / 400.0));
        return clamp((int) Math.round(currentMastery + 10 * (actual - expected)), 0, 100);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
