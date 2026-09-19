package com.titran.pingcortex.service;

import com.titran.pingcortex.ai.AiClientResolver;
import com.titran.pingcortex.repository.ConceptRepository;
import com.titran.pingcortex.repository.CourseMaterialRepository;
import com.titran.pingcortex.security.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AnalyzeCoverageService {
    private CourseMaterialRepository courseMaterialRepository;
    private ConceptRepository conceptRepository;
    private EncryptionService encryptionService;
    private AiClientResolver aiClientResolver;

    @Async("taskExecutor")
    @Transactional
    public void analyzeCoverage(UUID userId, UUID courseId, UUID conceptId) {

    }
}
