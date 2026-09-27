package com.titran.pingcortex.repository;

import com.titran.pingcortex.dto.response.ReviewScheduleResponse;
import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.util.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
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

    public Optional<ReviewScheduleResponse> findDueSchedule(UUID userId, UUID conceptId) {
        String sql = """
            SELECT interval_days, easiness_factor, repetition_count
            FROM review_schedule
            WHERE user_id = ? AND concept_id = ? AND next_review_at <= CURRENT_TIMESTAMP
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, userId);
            stmt.setObject(2, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(
                        new ReviewScheduleResponse(
                                rs.getInt("interval_days"),
                                rs.getDouble("easiness_factor"),
                                rs.getInt("repetition_count")
                        )
                ) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find due schedule", e);
        }
    }

    public void updateSchedule(UUID userId, UUID conceptId, int newIntervalDays, double newEasinessFactor, int newRepetitionCount) {
        String sql = """
            UPDATE review_schedule
            SET interval_days = ?, easiness_factor = ?, repetition_count = ?, next_review_at = CURRENT_TIMESTAMP + ? * INTERVAL '1 day'
            WHERE user_id = ? AND concept_id = ?
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setInt(1, newIntervalDays);
            stmt.setDouble(2, newEasinessFactor);
            stmt.setInt(3, newRepetitionCount);
            stmt.setInt(4, newIntervalDays);
            stmt.setObject(5, userId);
            stmt.setObject(6, conceptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update schedule", e);
        }
    }
}
