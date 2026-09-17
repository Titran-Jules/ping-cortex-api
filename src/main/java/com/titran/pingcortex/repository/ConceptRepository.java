package com.titran.pingcortex.repository;

import com.titran.pingcortex.dto.response.ConceptResponse;
import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.model.ConceptStatus;
import com.titran.pingcortex.util.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class ConceptRepository {
    private final DataSource dataSource;

    public ConceptResponse create(UUID courseId, String name, String description, int position) {
        String sql = """
            INSERT INTO concept (id, course_id, name, description, position)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, name, description, position, coverage_status
        """;
        ConceptResponse concept = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            UUID conceptId = UUID.randomUUID();
            stmt.setObject(1, conceptId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setInt(4, position);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    concept = conceptResponseRowMapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create the concept", e);
        }
        return concept;
    }

    public List<ConceptResponse> findAllByCourseId(UUID courseId) {
        String sql = """
            SELECT id, name, description, position, coverage_status
            FROM concept WHERE course_id = ?
            ORDER BY position
        """;
        List<ConceptResponse> conceptResponses = new ArrayList<>();
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conceptResponses.add(conceptResponseRowMapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find all concepts", e);
        }
        return conceptResponses;
    }

    private ConceptResponse conceptResponseRowMapper(ResultSet rs) throws SQLException {
        return new ConceptResponse(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("position"),
                ConceptStatus.valueOf(rs.getString("coverage_status"))
        );
    }
}
