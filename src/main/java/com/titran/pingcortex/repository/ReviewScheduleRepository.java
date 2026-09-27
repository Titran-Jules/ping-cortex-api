package com.titran.pingcortex.repository;

import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.util.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class ReviewScheduleRepository {
    private final DataSource dataSource;

    public void createInitial(UUID userId, UUID conceptId) {
        String sql = """
            INSERT INTO review_schedule (id, user_id, concept_id, next_review_at)
            VALUES (?, ?, ?, NOW() + '1 day')
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            UUID reviewId = UUID.randomUUID();
            stmt.setObject(1, reviewId);
            stmt.setObject(2, userId);
            stmt.setObject(3, conceptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialize review schedule", e);
        }
    }
}
