package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.response.ConceptMasteryResponse;
import com.titran.pingcortex.dto.response.ConceptResponse;
import com.titran.pingcortex.exception.ConceptNotFoundException;
import com.titran.pingcortex.exception.MasteryThresholdException;
import com.titran.pingcortex.repository.ConceptRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConceptService {
    private final ConceptRepository conceptRepository;

    public List<ConceptResponse> findAll(UUID userId, UUID courseId) {
        return conceptRepository.findAllByCourseId(userId, courseId);
    }

    public ConceptResponse confirmConceptCoverage(UUID userId, UUID courseId, UUID conceptId) {
        return conceptRepository.confirmCoverage(userId, courseId, conceptId)
                .orElseThrow(ConceptNotFoundException::new);
    }

    public ConceptResponse revertConceptCoverage(UUID userId, UUID courseId, UUID conceptId) {
        return conceptRepository.revertCoverage(userId, courseId, conceptId)
                .orElseThrow(ConceptNotFoundException::new);
    }

    public ConceptResponse advanceConceptCoverage(UUID userId, UUID courseId, UUID conceptId) {
        ConceptResponse conceptToAdvance = conceptRepository.findById(conceptId)
                .orElseThrow(ConceptNotFoundException::new);
        if (conceptToAdvance.position() > 1) {
            UUID previousConceptId = conceptRepository.findPreviousConceptId(courseId, conceptToAdvance.position())
                    .orElseThrow(ConceptNotFoundException::new);
            int previousMasteryLevel = conceptRepository.findConceptMastery(userId, previousConceptId)
                    .map(ConceptMasteryResponse::masteryLevel)
                    .orElse(0);

            if (previousMasteryLevel < 80) {
                throw new MasteryThresholdException();
            }
        }
        return conceptRepository.advanceCoverage(userId, courseId, conceptId)
                .orElseThrow(ConceptNotFoundException::new);
    }

    public ConceptMasteryResponse findConceptMastery(UUID userId, UUID conceptId) {
        return conceptRepository.findConceptMastery(userId, conceptId)
                .orElseThrow(ConceptNotFoundException::new);
    }
}
