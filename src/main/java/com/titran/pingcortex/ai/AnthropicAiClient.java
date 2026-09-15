package com.titran.pingcortex.ai;

import com.titran.pingcortex.exception.AiProviderException;
import com.titran.pingcortex.model.Difficulty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.titran.pingcortex.ai.AiResults.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("anthropic")
public class AnthropicAiClient implements AiClient {
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

    @Override
    public List<ConceptSuggestion> analyzeCourse(String apiKey, String courseTitle, String courseDescription, List<String> materialContent) {
        String systemPrompt = """
            Tu es un assistant pedagogique. Decoupe un cours en concepts cles,
            ordonnes du plus fondamental au plus avance, adaptes au niveau indique.
            Reponds UNIQUEMENT avec un tableau JSON valide, sans aucun texte avant
            ou apres, au format exact :
            [{"name": "...", "description": "..."}]
        """;

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Titre du cours : ").append(courseTitle).append("\n");
        userPrompt.append("Description : ").append(courseDescription).append("\n");
        if (!materialContent.isEmpty()) {
            userPrompt.append("Support de cours fournis :\n");
            for (String content : materialContent) {
                userPrompt.append("---\n").append(content).append("\n");
            }
        }
        String rawResponse = callApi(apiKey,systemPrompt,userPrompt.toString());
        return parseJson(rawResponse, new TypeReference<List<ConceptSuggestion>>() {});
    }

    @Override
    public QuizQuestionSuggestion generateQuiz(String apiKey, String conceptName, String conceptDescription, Difficulty difficulty) {
        String systemPrompt = """
            Tu generes une question a choix multiples de difficulte %s sur le
            concept suivant : "%s" - %s.
            Exactement 4 options, une seule correcte. Reponds UNIQUEMENT en JSON,
            sans texte avant ou apres, au format exact :
            {"questionText": "...", "options": [{"text": "...", "correct": true|false}, ...]}
        """.formatted(difficulty, conceptName, conceptDescription);

        String rawResponse = callApi(apiKey, systemPrompt, "Genere la question suivante.");
        Map<String, Object> parsed = parseJson(rawResponse, new TypeReference<>() {});

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawOptions = (List<Map<String, Object>>) parsed.get("options");
        List<QuizOptionSuggestion> options = new ArrayList<>();
        for (Map<String, Object> option : rawOptions) {
            options.add(new QuizOptionSuggestion(
                    (String) option.get("text"),
                    (Boolean) option.get("correct")
            ));
        }
        return new QuizQuestionSuggestion((String) parsed.get("questionText"), difficulty, options);
    }

    @Override
    public FeynmanEvaluationResult evaluateFeynman(String apiKey, String conceptName, String conceptDescription, String explanationText) {
        String systemPrompt = """
            Tu evalues l'explication d'un eleve sur un concept, selon la methode
            Feynman (l'eleve doit expliquer avec ses propres mots comme s'il
            enseignait a quelqu'un d'autre). Concept : "%s" - %s.
            Evalue sur plusieurs criteres (clarte, exactitude, completude).
            Reponds UNIQUEMENT en JSON, sans texte avant ou apres, au format exact :
            {"criteria": [{"name": "...", "score": 0-100, "comment": "..."}],
            "overallFeedback": "...", "misconceptions": ["..."], "score": 0-100}
        """.formatted(conceptName, conceptDescription);

        String rawResponse = callApi(apiKey, systemPrompt, explanationText);
        return parseJson(rawResponse, new TypeReference<FeynmanEvaluationResult>() {});
    }

    @Override
    public String chat(String apiKey, String courseContext, List<ChatTurn> history, String newMessage) {
        String systemPrompt = """
            Tu es un tuteur pedagogique pour ce cours : %s.
            Reponds de facon claire et encourageante, adaptee au niveau de l'eleve.
            Pas de format JSON ici - reponse en texte libre, conversationnel.
        """.formatted(courseContext);

        List<ChatTurn> messages = new ArrayList<>();
        messages.add(new ChatTurn("user", newMessage));
        return callApi(apiKey, systemPrompt, messages);
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

    private String callApi(String apiKey, String systemPrompt, String userMessage) {
        return callApi(apiKey, systemPrompt, List.of(new ChatTurn("user", userMessage)));
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
        } catch (JacksonException e) {
            throw new AiProviderException("Failed to parse AI response as JSON", e);
        }
    }

    private record AnthropicResponse(List<ContentBlock> content) {}
    private record ContentBlock(String type, String text) {}
}
