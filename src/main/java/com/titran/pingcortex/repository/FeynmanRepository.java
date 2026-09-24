package com.titran.pingcortex.repository;

import com.titran.pingcortex.ai.AiResults;
import com.titran.pingcortex.dto.response.FeynmanSubmissionResponse;
import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.util.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class FeynmanRepository {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public FeynmanSubmissionResponse create(UUID userId, UUID conceptId, String explanationText, AiResults.FeynmanEvaluationResult evaluation, double score) {
        String sql = """
            INSERT INTO feynman_submission (id, user_id, concept_id, explanation_text, ai_evaluation, score)
            VALUES (?, ?, ?, ?, ?::jsonb, ?)
            RETURNING id, explanation_text, ai_evaluation, score, created_at
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            UUID submissionId = UUID.randomUUID();
            stmt.setObject(1, submissionId);
            stmt.setObject(2, userId);
            stmt.setObject(3, conceptId);
            stmt.setString(4, explanationText);
            stmt.setString(5, toJson(evaluation));
            stmt.setDouble(6, score);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return feynmanSubmissionRowMapper(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert feynman_submission", e);
        }
    }

    public Optional<Instant> findLastSubmissionDate(UUID userId, UUID conceptId) {
        String sql = """
            SELECT MAX(created_at) AS last_submission_at
            FROM feynman_submission
            WHERE user_id = ? AND concept_id = ?
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, userId);
            stmt.setObject(2, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var timestamp = rs.getTimestamp("last_submission_at");
                    return timestamp != null ? Optional.of(timestamp.toInstant()) : Optional.empty();
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find last Feynman submission date", e);
        }
    }

    private String toJson(AiResults.FeynmanEvaluationResult evaluation) {
        try {
            return objectMapper.writeValueAsString(evaluation);
        } catch (Exception e) {
            throw new DataAccessException("Failed to serialize ai_evaluation", e);
        }
    }

    private FeynmanSubmissionResponse feynmanSubmissionRowMapper(ResultSet rs) throws SQLException {
        String rawEvaluation = rs.getString("ai_evaluation");
        AiResults.FeynmanEvaluationResult evaluation = objectMapper.readValue(rawEvaluation, AiResults.FeynmanEvaluationResult.class);
        return new FeynmanSubmissionResponse(
                rs.getObject("id", UUID.class),
                rs.getString("explanation_text"),
                evaluation,
                rs.getDouble("score"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
