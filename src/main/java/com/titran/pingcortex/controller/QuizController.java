package com.titran.pingcortex.controller;

import com.titran.pingcortex.dto.response.QuizQuestionResponse;
import com.titran.pingcortex.security.CurrentUser;
import com.titran.pingcortex.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping("/concepts/{conceptId}/quiz")
    public ResponseEntity<List<QuizQuestionResponse>> startQuiz(@PathVariable UUID conceptId, HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.ok(quizService.selectQuizQuestions(userId, conceptId));
    }
}
