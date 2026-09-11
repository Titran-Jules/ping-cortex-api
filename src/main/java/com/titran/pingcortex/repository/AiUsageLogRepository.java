package com.titran.pingcortex.repository;

import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.model.UsageAggregate;
import com.titran.pingcortex.util.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class AiUsageLogRepository {
    private final DataSource dataSource;

    public UsageAggregate getAggregateForUser(UUID userId) {
        String sql = """
            SELECT COALESCE(SUM(token_in), 0) AS total_in,
                   COALESCE(SUM(token_out), 0) AS total_out,
                   COUNT(*) AS request_count
            FROM ai_usage_log
            WHERE user_id = ?
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return new UsageAggregate(
                        rs.getLong("total_in"),
                        rs.getLong("total_out"),
                        rs.getLong("request_count")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to aggregate AI usage", e);
        }
    }
}
