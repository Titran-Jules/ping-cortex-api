package com.titran.pingcortex.controller;

import com.titran.pingcortex.dto.request.FeynmanRequest;
import com.titran.pingcortex.dto.response.FeynmanSubmissionResponse;
import com.titran.pingcortex.security.CurrentUser;
import com.titran.pingcortex.service.FeynmanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class FeynmanController {
    private final FeynmanService feynmanService;

    @PostMapping("/concepts/{conceptId}/feynman-submissions")
    public ResponseEntity<FeynmanSubmissionResponse> feynmanSubmissions(@PathVariable UUID conceptId, @RequestBody FeynmanRequest feynmanRequest, HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feynmanService.feynmanSubmission(userId, conceptId, feynmanRequest.explanationText()));
    }
}
