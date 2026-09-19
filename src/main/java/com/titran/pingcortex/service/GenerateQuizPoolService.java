package com.titran.pingcortex.service;

import com.titran.pingcortex.ai.AiClient;
import com.titran.pingcortex.ai.AiClientResolver;
import com.titran.pingcortex.ai.AiResults;
import com.titran.pingcortex.dto.response.ConceptResponse;
import com.titran.pingcortex.dto.response.QuizQuestionResponse;
import com.titran.pingcortex.exception.ConceptNotFoundException;
import com.titran.pingcortex.exception.UserNotFoundException;
import com.titran.pingcortex.model.AiProvider;
import com.titran.pingcortex.model.Difficulty;
import com.titran.pingcortex.model.UserApiKey;
import com.titran.pingcortex.repository.ConceptRepository;
import com.titran.pingcortex.repository.QuizRepository;
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
public class GenerateQuizPoolService {
    private final ConceptRepository conceptRepository;
    private final QuizRepository quizRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;
    private final AiClientResolver aiClientResolver;

    @Async("taskExecutor")
    @Transactional
    public void generateQuizPool(UUID userId, UUID conceptId, Difficulty difficulty, int count) {
        if (quizRepository.countByConceptId(conceptId) > 0) {
            throw new IllegalStateException("Concept with id " + conceptId + " already has quiz pool");
        } else {
            UserApiKey activeKey = userRepository.findActiveApiKey(userId)
                    .orElseThrow(UserNotFoundException::new);
            String decryptedKey = encryptionService.decrypt(activeKey.encryptedKey());
            AiProvider provider = AiProvider.valueOf(activeKey.provider());
            AiClient aiClient = aiClientResolver.resolve(provider);
            ConceptResponse concept = conceptRepository.findById(conceptId)
                            .orElseThrow(ConceptNotFoundException::new);
            List<AiResults.QuizQuestionSuggestion> suggestions = aiClient.generateQuizBatch(decryptedKey, concept.name(), concept.description(), difficulty, count);

            for (AiResults.QuizQuestionSuggestion suggestion : suggestions) {
                QuizQuestionResponse quizQuestion = quizRepository.createQuizQuestion(conceptId, suggestion.questionText(), difficulty);
                for (AiResults.QuizOptionSuggestion optionSuggestion : suggestion.options()) {
                    quizRepository.createQuizOption(quizQuestion.id(), optionSuggestion.text(), optionSuggestion.correct());
                }
            }
        }
    }
}
