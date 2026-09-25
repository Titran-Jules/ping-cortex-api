package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.response.ChatMessagesResponse;
import com.titran.pingcortex.dto.response.ChatSessionResponse;
import com.titran.pingcortex.dto.response.CourseResponse;
import com.titran.pingcortex.exception.CourseNotFoundException;
import com.titran.pingcortex.repository.ChatAiRepository;
import com.titran.pingcortex.repository.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ChatAiService {
    private final ChatAiRepository chatAiRepository;
    private final CourseRepository courseRepository;

    public ChatSessionResponse createSession(UUID userId, UUID courseId) {
        CourseResponse checkCourseOfThisUser = courseRepository.findById(userId, courseId)
                .orElseThrow(CourseNotFoundException::new);
        return chatAiRepository.createSession(userId, courseId);
    }

    public List<ChatMessagesResponse> findAllMessagesBySessionId(UUID userId, UUID sessionId) {
        return chatAiRepository.findAllMessagesByCourseId(userId, sessionId);
    }
}
