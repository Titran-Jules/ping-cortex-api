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
public class MasteryRepository {
    private final DataSource dataSource;

    public void updateMasteryLevel(UUID userId, UUID conceptId, int newMastery) {
        String sql = """
            UPDATE user_concept_mastery
            SET mastery_level = ?,
                last_reviewed_at = CURRENT_TIMESTAMP
            WHERE user_id = ? AND concept_id = ?
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setInt(1, newMastery);
            stmt.setObject(2, userId);
            stmt.setObject(3, conceptId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("No mastery row found to update for user " + userId + " and concept " + conceptId, null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update mastery level", e);
        }
    }
}
