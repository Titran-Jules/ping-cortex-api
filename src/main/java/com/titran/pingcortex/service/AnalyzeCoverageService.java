package com.titran.pingcortex.service;

import com.titran.pingcortex.ai.AiClient;
import com.titran.pingcortex.ai.AiClientResolver;
import com.titran.pingcortex.ai.AiResults;
import com.titran.pingcortex.dto.response.ConceptResponse;
import com.titran.pingcortex.exception.CourseMaterialNotFoundException;
import com.titran.pingcortex.exception.UserNotFoundException;
import com.titran.pingcortex.model.AiProvider;
import com.titran.pingcortex.model.UserApiKey;
import com.titran.pingcortex.repository.ConceptRepository;
import com.titran.pingcortex.repository.CourseMaterialRepository;
import com.titran.pingcortex.repository.UserRepository;
import com.titran.pingcortex.security.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AnalyzeCoverageService {
    private CourseMaterialRepository courseMaterialRepository;
    private UserRepository userRepository;
    private ConceptRepository conceptRepository;
    private EncryptionService encryptionService;
    private AiClientResolver aiClientResolver;

    @Async("taskExecutor")
    @Transactional
    public void analyzeCoverage(UUID userId, UUID courseId, UUID materialId) {
        var courseMaterial = courseMaterialRepository.findById(materialId)
                .orElseThrow(CourseMaterialNotFoundException::new);
        List<ConceptResponse> concepts = conceptRepository.findAllByCourseId(userId, courseId);
        UserApiKey activeKey = userRepository.findActiveApiKey(userId)
                .orElseThrow(UserNotFoundException::new);
        String decryptedKey = encryptionService.decrypt(activeKey.encryptedKey());
        AiProvider aiProvider = AiProvider.valueOf(activeKey.provider().toUpperCase());
        AiClient aiClient = aiClientResolver.resolve(aiProvider);

        String materialContent = courseMaterial.content();
        List<AiResults.ConceptSummary> conceptSummaries = concepts.stream()
                .map(c -> new AiResults.ConceptSummary(c.id(), c.name(), c.description()))
                .toList();

        List<AiResults.ConceptCoverageSuggestion> suggestions = aiClient.analyzeCoverage(decryptedKey, conceptSummaries, materialContent);
        for (AiResults.ConceptCoverageSuggestion suggestion : suggestions) {
            conceptRepository.addSuggestionCoverage(userId, courseId, suggestion.conceptId(), courseMaterial.id());
        }
    }
}
