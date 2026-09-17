package com.titran.pingcortex.controller;

import com.titran.pingcortex.dto.response.ConceptResponse;
import com.titran.pingcortex.security.CurrentUser;
import com.titran.pingcortex.service.ConceptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ConceptController {
    private final ConceptService conceptService;

    @GetMapping("/courses/{courseId}/concepts")
    public ResponseEntity<List<ConceptResponse>> findAllByCourseId(@PathVariable UUID courseId, HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.ok(conceptService.findAll(userId, courseId));
    }

    @PostMapping("/courses/{courseId}/concepts/{conceptId}/confirm-coverage")
    public ResponseEntity<ConceptResponse> confirmCoverage(@PathVariable UUID courseId, @PathVariable UUID conceptId, HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.ok(conceptService.confirmConceptCoverage(userId, courseId, conceptId));
    }

    @PostMapping("/courses/{courseId}/concepts/{conceptId}/revert-coverage")
    public ResponseEntity<ConceptResponse> revertCoverage(@PathVariable UUID courseId, @PathVariable UUID conceptId, HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.ok(conceptService.revertConceptCoverage(userId, courseId, conceptId));
    }
}
