package com.titran.pingcortex.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.titran.pingcortex.exception.AiProviderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;
import com.titran.pingcortex.ai.AiResults.*;

import java.util.List;
import java.util.Map;

@Service("anthropic")
public class AnthropicAiClient extends AiClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;

    public AnthropicAiClient(
            @Value("${ai.anthropic.base-url}") String baseUrl,
            @Value("${ai.anthropic.version}") String anthropicVersion,
            @Value("${ai.anthropic.model}") String model,
            @Value("${ai.anthropic.max-tokens}") int maxTokens,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("anthropic-version", anthropicVersion)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    private AiProviderException translateError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        if (status == 401) {
            return new AiProviderException("Invalid or revoked Anthropic API key", e);
        }
        if (status == 429) {
            return new AiProviderException("Anthropic rate limit exceeded, try again later", e);
        }
        return new AiProviderException("Anthropic API key error (HTTP " + status + ")", e);
    }

    private String callApi(String apiKey, String systemPrompt, List<ChatTurn> messages) {
        try {
            List<Map<String, String>> anthropicMessages = messages.stream()
                    .map(turn -> Map.of("role", turn.role().toLowerCase(), "content", turn.content()))
                    .toList();
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "system", systemPrompt,
                    "messages", anthropicMessages
            );
            AnthropicResponse response = restClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(AnthropicResponse.class);

            if (response == null || response.content().isEmpty()) {
                throw new AiProviderException("Anthropic returned an empty response", null);
            }
            return response.content().get(0).text();
        } catch (RestClientResponseException e) {
            throw translateError(e);
        }
    }

    private <T> T parseJson(String rawText, TypeReference<T> typeRef) {
        try {
            String cleaned = rawText.strip();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```(json)?\\s*", "").replaceAll("```\\s*$", "");
            }
            return objectMapper.readValue(cleaned, typeRef);
        } catch (JsonProcessingException e) {
            throw new AiProviderException("Failed to parse AI response as JSON", e);
        }
    }

    private record AnthropicResponse(List<ContentBlock> content) {}
    private record ContentBlock(String type, String text) {}
}
