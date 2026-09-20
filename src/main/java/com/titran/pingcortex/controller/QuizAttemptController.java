package com.titran.pingcortex.controller;

import com.titran.pingcortex.dto.request.QuizSelectedOption;
import com.titran.pingcortex.dto.response.QuizAttemptResponse;
import com.titran.pingcortex.security.CurrentUser;
import com.titran.pingcortex.service.QuizAttemptService;
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
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;

    @PostMapping("/quiz-questions/{quizQuestionId}/attempts")
    public ResponseEntity<QuizAttemptResponse> submitAttempt(@PathVariable UUID quizQuestionId, HttpServletRequest request, @RequestBody QuizSelectedOption quizSelectedOption) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizAttemptService.getQuizQuestionAfterAttempt(userId, quizQuestionId, quizSelectedOption));
    }
}
