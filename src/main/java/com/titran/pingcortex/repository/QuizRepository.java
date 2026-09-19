package com.titran.pingcortex.repository;

import com.titran.pingcortex.dto.response.QuizOptionResponse;
import com.titran.pingcortex.dto.response.QuizQuestionResponse;
import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.model.Difficulty;
import com.titran.pingcortex.util.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class QuizRepository {
    private final DataSource dataSource;

    public QuizQuestionResponse createQuizQuestion(UUID conceptId, String questionText, Difficulty difficulty) {
        String sql = """
            INSERT INTO quiz_question (id, concept_id, question_text, difficulty)
            VALUES (?, ?, ?, ?)
            RETURNING id, question_text, difficulty;
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            UUID quizId = UUID.randomUUID();
            stmt.setObject(1, quizId);
            stmt.setObject(2, conceptId);
            stmt.setString(3, questionText);
            stmt.setString(4, difficulty.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new QuizQuestionResponse(quizId, questionText, difficulty, new ArrayList<>());
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert quiz_question", e);
        }
    }

    public QuizOptionResponse createQuizOption(UUID questionId, String text, boolean isCorrect) {
        String sql = """
            INSERT INTO quiz_option (id, question_id, text, is_correct)
            VALUES (?, ?, ?, ?)
            RETURNING id, question_id, text, is_correct;
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            UUID optionId = UUID.randomUUID();
            stmt.setObject(1, optionId);
            stmt.setObject(2, questionId);
            stmt.setString(3, text);
            stmt.setBoolean(4, isCorrect);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new QuizOptionResponse(optionId, text);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert quiz_option", e);
        }
    }

    public int countByConceptId(UUID conceptId) {
        String sql = """
            SELECT COUNT(*) AS count
            FROM quiz_questions
            WHERE concept_id = ?;
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("count") : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count questions", e);
        }
    }
}
