package com.titran.pingcortex.dto.response;

public record ChatExchangeResponse(ChatMessagesResponse userMessage, ChatMessagesResponse assistantMessage) {
}
