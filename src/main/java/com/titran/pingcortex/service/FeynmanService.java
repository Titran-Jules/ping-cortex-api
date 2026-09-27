package com.titran.pingcortex.service;

import com.titran.pingcortex.ai.AiClient;
import com.titran.pingcortex.ai.AiClientResolver;
import com.titran.pingcortex.ai.AiResults;
import com.titran.pingcortex.dto.response.ConceptResponse;
import com.titran.pingcortex.dto.response.FeynmanSubmissionResponse;
import com.titran.pingcortex.exception.ConceptNotAvailableException;
import com.titran.pingcortex.exception.ConceptNotFoundException;
import com.titran.pingcortex.exception.NoActiveKeyException;
import com.titran.pingcortex.model.AiProvider;
import com.titran.pingcortex.model.UserApiKey;
import com.titran.pingcortex.repository.*;
import com.titran.pingcortex.security.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class FeynmanService {
    private final UserRepository userRepository;
    private final FeynmanRepository feynmanRepository;
    private final ConceptRepository conceptRepository;
    private final AiClientResolver aiClientResolver;
    private final EncryptionService encryptionService;
    private final QuizRepository quizRepository;
    private final MasteryRepository masteryRepository;
    private final ReviewScheduleService reviewScheduleService;

    @Transactional
    public FeynmanSubmissionResponse feynmanSubmission(UUID userId, UUID conceptId, String explanationText) {
        if (!conceptRepository.belongsToUser(userId, conceptId)) {
            throw new ConceptNotFoundException();
        }
        if (!conceptRepository.isCoveredOrAnticipated(conceptId)) {
            throw new ConceptNotAvailableException();
        }
        ConceptResponse concept = conceptRepository.findById(conceptId)
                .orElseThrow(ConceptNotFoundException::new);
        UserApiKey activeKey = userRepository.findActiveApiKey(userId)
                .orElseThrow(NoActiveKeyException::new);
        String decryptedKey = encryptionService.decrypt(activeKey.encryptedKey());
        AiProvider provider = AiProvider.valueOf(activeKey.provider().toUpperCase());
        AiClient aiClient = aiClientResolver.resolve(provider);

        AiResults.FeynmanEvaluationResult result = aiClient.evaluateFeynman(decryptedKey, concept.name(), concept.description(), explanationText);

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
