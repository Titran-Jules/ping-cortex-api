package com.titran.pingcortex.ai;

import com.titran.pingcortex.ai.AiResults.*;
import com.titran.pingcortex.exception.AiProviderException;
import com.titran.pingcortex.model.Difficulty;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractAiClient implements AiClient {
    protected final ObjectMapper objectMapper;

    protected AbstractAiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected abstract String callProviderApi(String apiKey, String systemPrompt, List<ChatTurn> messages, boolean jsonOutput);

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
        String rawResponse = callProviderApi(apiKey, systemPrompt, List.of(new ChatTurn("user", userPrompt.toString())), true);
        return parseJson(rawResponse, new TypeReference<List<ConceptSuggestion>>() {});
    }

    @Override
    public List<ConceptCoverageSuggestion> analyzeCoverage(String apiKey, List<ConceptSummary> concepts, String materialContent) {
        String systemPrompt = """
            Tu es un assistant pedagogique.
            Tu as élaboré ces CONCEPTS pour un cours.
            Tu va ensuite analyser ces concepts à partir du MATERIAL_CONTENT et proposer les concepts qui pourront être mis à COVERED
            Réponds UNIQUEMENT en JSON valide, sans aucun texte devant ou apres, au format exact :
            [{"conceptId": "...", "confidence": "HIGH/MEDIUM/LOW"}]
        """;

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("CONCEPTS : ").append("\n");
        for (ConceptSummary concept : concepts) {
            userPrompt.append("id : ").append(concept.id()).append("; name: ").append(concept.name()).append("; description: ").append(concept.description()).append(";\n");
        }
        userPrompt.append("\n");
        userPrompt.append("MATERIAL CONTENT : ").append("\n");
        userPrompt.append(materialContent).append("\n");
        String rawResponse = callProviderApi(apiKey, systemPrompt, List.of(new ChatTurn("user", userPrompt.toString())), true);
        return parseJson(rawResponse, new TypeReference<List<ConceptCoverageSuggestion>>() {});
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

        String rawResponse = callProviderApi(apiKey, systemPrompt, List.of(new ChatTurn("user", "Genere la question suivante.")), true);
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

        String rawResponse = callProviderApi(apiKey, systemPrompt, List.of(new ChatTurn("user", explanationText)), true);
        return parseJson(rawResponse, new TypeReference<FeynmanEvaluationResult>() {});
    }

    @Override
    public String chat(String apiKey, String courseContext, List<ChatTurn> history, String newMessage) {
        String systemPrompt = """
            Tu es un tuteur pedagogique pour ce cours : %s.
            Reponds de facon claire et encourageante, adaptee au niveau de l'eleve.
            Pas de format JSON ici - reponse en texte libre, conversationnel.
        """.formatted(courseContext);

        List<ChatTurn> messages = new ArrayList<>(history);
        messages.add(new ChatTurn("user", newMessage));
        return callProviderApi(apiKey, systemPrompt, messages, false);
    }

    protected <T> T parseJson(String rawText, TypeReference<T> typeRef) {
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
}
