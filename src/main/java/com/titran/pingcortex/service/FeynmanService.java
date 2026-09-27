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

import java.util.UUID;

@Service
@AllArgsConstructor
public class FeynmanService {
    private final UserRepository userRepository;
    private final ConceptRepository conceptRepository;
    private final AiClientResolver aiClientResolver;
    private final EncryptionService encryptionService;
    private final FeynmanPersistenceService feynmanPersistenceService;

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

        return feynmanPersistenceService.persistSubmission(userId, conceptId, explanationText, result);
    }
}
