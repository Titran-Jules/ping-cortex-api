package com.titran.pingcortex.repository;

import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.util.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class QuizAttemptRepository {
    private final DataSource dataSource;

    public void create(UUID userId,UUID questionId, UUID selectedOptionId, boolean isCorrect) {
        String sql = """
            INSERT INTO quiz_attempt (id, user_id, question_id, selected_option_id, is_correct)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            UUID attemptId = UUID.randomUUID();
            stmt.setObject(1, attemptId);
            stmt.setObject(2, userId);
            stmt.setObject(3, questionId);
            stmt.setObject(4, selectedOptionId);
            stmt.setBoolean(5, isCorrect);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert quiz attempt", e);
        }
    }
}
