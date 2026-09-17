package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.response.ConceptResponse;
import com.titran.pingcortex.exception.ConceptNotFoundException;
import com.titran.pingcortex.repository.ConceptRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConceptService {
    private final ConceptRepository conceptRepository;

    public List<ConceptResponse> findAll(UUID courseId) {
        return conceptRepository.findAllByCourseId(courseId);
    }

    public ConceptResponse confirmConceptCoverage(UUID courseId, UUID conceptId) {
        return conceptRepository.confirmCoverage(courseId, conceptId)
                .orElseThrow(ConceptNotFoundException::new);
    }

    public ConceptResponse revertConceptCoverage(UUID courseId, UUID conceptId) {
        return conceptRepository.revertCoverage(courseId, conceptId)
                .orElseThrow(ConceptNotFoundException::new);
    }
}
