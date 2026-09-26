package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.response.ChatExchangeResponse;
import com.titran.pingcortex.dto.response.ChatMessagesResponse;
import com.titran.pingcortex.model.Role;
import com.titran.pingcortex.repository.ChatAiRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ChatExchangePersistenceService {
    private final ChatAiRepository chatAiRepository;

    @Transactional
    public ChatExchangeResponse persistExchange(UUID sessionId, String newMessage, String aiResponse) {
        ChatMessagesResponse userMessage = chatAiRepository.insertNewMessage(sessionId, Role.USER, newMessage);
        ChatMessagesResponse assistantMessage = chatAiRepository.insertNewMessage(sessionId, Role.ASSISTANT, aiResponse);
        return new ChatExchangeResponse(userMessage, assistantMessage);
    }
}
