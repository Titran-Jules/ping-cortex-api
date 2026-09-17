package com.titran.pingcortex.controller;

import com.titran.pingcortex.dto.response.ConceptResponse;
import com.titran.pingcortex.service.ConceptService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ConceptController {
    private final ConceptService conceptService;

    @GetMapping("/courses/{courseId}/concepts")
    public ResponseEntity<List<ConceptResponse>> findAllByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(conceptService.findAll(courseId));
    }
}
