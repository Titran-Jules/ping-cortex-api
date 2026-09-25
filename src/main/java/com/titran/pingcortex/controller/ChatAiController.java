package com.titran.pingcortex.controller;

import com.titran.pingcortex.dto.response.ChatSessionResponse;
import com.titran.pingcortex.security.CurrentUser;
import com.titran.pingcortex.service.ChatAiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class ChatAiController {
    private final ChatAiService chatAiService;

    @PostMapping("/courses/{courseId}/chat-sessions")
    public ResponseEntity<ChatSessionResponse> createSession(@PathVariable UUID courseId, HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatAiService.createSession(courseId, userId));
    }
}
