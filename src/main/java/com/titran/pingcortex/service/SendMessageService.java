package com.titran.pingcortex.service;

import com.titran.pingcortex.ai.AiClient;
import com.titran.pingcortex.ai.AiClientResolver;
import com.titran.pingcortex.ai.AiResults;
import com.titran.pingcortex.dto.response.ChatMessagesResponse;
import com.titran.pingcortex.dto.response.CourseResponse;
import com.titran.pingcortex.exception.CourseNotFoundException;
import com.titran.pingcortex.exception.NoActiveKeyException;
import com.titran.pingcortex.model.AiProvider;
import com.titran.pingcortex.model.Role;
import com.titran.pingcortex.model.UserApiKey;
import com.titran.pingcortex.repository.ChatAiRepository;
import com.titran.pingcortex.repository.UserRepository;
import com.titran.pingcortex.security.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SendMessageService {
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final ChatAiRepository chatAiRepository;
    private final AiClientResolver aiClientResolver;

    public String sendMessageToAi(UUID userId, UUID sessionId, String message) {
        UserApiKey activeKey = userRepository.findActiveApiKey(userId)
                .orElseThrow(NoActiveKeyException::new);
        String decryptedKey = encryptionService.decrypt(activeKey.encryptedKey());
        List<ChatMessagesResponse> findHistory = chatAiRepository.findAllMessagesBySessionId(userId, sessionId);

        AiProvider provider = AiProvider.valueOf(activeKey.provider().toUpperCase());
        AiClient aiClient = aiClientResolver.resolve(provider);

        List<AiResults.ChatTurn> history = findHistory.stream()
                .map(m -> new AiResults.ChatTurn(m.role().name(), m.content()))
                .toList();

        CourseResponse course = chatAiRepository.findCourseBySessionId(userId, sessionId)
                .orElseThrow(CourseNotFoundException::new);

        String courseContext = "Title : " + course.title() + ". Description : " + course.description();

        return aiClient.chat(decryptedKey, courseContext, history, message);
    }
}
