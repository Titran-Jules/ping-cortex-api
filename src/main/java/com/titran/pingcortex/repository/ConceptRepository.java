package com.titran.pingcortex.repository;

import com.titran.pingcortex.dto.response.ConceptMasteryResponse;
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
import java.util.Optional;
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
            stmt.setObject(2, courseId);
            stmt.setString(3, name);
            stmt.setString(4, description);
            stmt.setInt(5, position);
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

    public List<ConceptResponse> findAllByCourseId(UUID userId, UUID courseId) {
        String sql = """
            SELECT c.id, c.name, c.description, c.position, c.coverage_status
            FROM concept c
            JOIN course co ON c.course_id = co.id
            WHERE c.course_id = ? AND co.user_id = ?
            ORDER BY position
        """;
        List<ConceptResponse> conceptResponses = new ArrayList<>();
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, courseId);
            stmt.setObject(2, userId);
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

    public Optional<ConceptResponse> confirmCoverage(UUID userId, UUID courseId, UUID conceptId) {
        return updateCoverageStatus(userId, courseId, conceptId, "COVERED");
    }

    public Optional<ConceptResponse> revertCoverage(UUID userId, UUID courseId, UUID conceptId) {
        return updateCoverageStatus(userId, courseId, conceptId, "NOT_COVERED");
    }

    public Optional<ConceptResponse> advanceCoverage(UUID userId, UUID courseId, UUID conceptId) {
        return updateCoverageStatus(userId, courseId, conceptId, "ANTICIPATED_COVERED");
    }

    private Optional<ConceptResponse> updateCoverageStatus(UUID userId, UUID courseId, UUID conceptId, String newStatus) {
        String sql = """
            UPDATE concept c
            SET coverage_status = ?::coverage_status_list
            FROM course co
            WHERE c.course_id = co.id
              AND c.id = ?
              AND c.course_id = ?
              AND co.user_id = ?
            RETURNING c.id, c.name, c.description, c.position, c.coverage_status
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setString(1, newStatus);
            stmt.setObject(2, conceptId);
            stmt.setObject(3, courseId);
            stmt.setObject(4, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(conceptResponseRowMapper(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update concept coverage status", e);
        }
    }

    public Optional<ConceptMasteryResponse> findConceptMastery(UUID userId, UUID conceptId) {
        String sql = """
            SELECT concept_id, mastery_level, last_reviewed_at
            FROM user_concept_mastery
            WHERE user_id = ? AND concept_id = ?
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, userId);
            stmt.setObject(2, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(conceptMasteryResponseRowMapper(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find concept mastery", e);
        }
    }

    public boolean updateSuggestedCoverage(UUID userId, UUID courseId, UUID conceptId, UUID materialId) {
        String sql = """
            UPDATE concept c
            SET suggested_coverage = TRUE,
                suggested_from_material_id = ?
            FROM course co
            WHERE c.course_id = co.id
                AND c.id = ?
                AND co.course_id = ?
                AND co.user_id = ?
                AND c.coverage_status != 'COVERAGE'
            RETURNING c.id, c.name, c.description, c.position, c.coverage_status 
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, materialId);
            stmt.setObject(2, conceptId);
            stmt.setObject(3, courseId);
            stmt.setObject(4, userId);
            int result = stmt.executeUpdate();
            if (result != 1) {
                return false;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update concept suggested_coverage", e);
        }
        return true;
    }

    public boolean declineSuggestedCoverage(UUID userId, UUID courseId, UUID conceptId, UUID materialId) {
        String sql = """
            UPDATE concept c
            SET suggested_coverage = FALSE,
                suggested_from_material_id = ?
            FROM course co
            WHERE c.course_id = co.id
                AND c.id = ?
                AND co.course_id = ?
                AND co.user_id = ?
                AND c.coverage_status != 'COVERAGE'
            RETURNING c.id, c.name, c.description, c.position, c.coverage_status 
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, materialId);
            stmt.setObject(2, conceptId);
            stmt.setObject(3, courseId);
            stmt.setObject(4, userId);
            int result = stmt.executeUpdate();
            if (result != 1) {
                return false;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to decline concept suggested_coverage", e);
        }
        return true;
    }

    public List<ConceptResponse> findAllSuggestedCoverageConcept(UUID userId, UUID courseId) {
        String sql = """
            SELECT  c.id, c.name, c.description, c.position, c.coverage_status
            FROM concept c
            JOIN course co
            ON c.course_id = co.id
            WHERE c.course_id = ? AND co.user_id = ? AND c.suggested_coverage = TRUE AND c.coverage_status != 'COVERAGE'
        """;
        List<ConceptResponse> conceptResponses = new ArrayList<>();
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, courseId);
            stmt.setObject(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    conceptResponses.add(conceptResponseRowMapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find concept suggested_coverage", e);
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

    private ConceptMasteryResponse conceptMasteryResponseRowMapper(ResultSet rs) throws SQLException {
        return new ConceptMasteryResponse(
                rs.getObject("concept_id", UUID.class),
                rs.getInt("mastery_level"),
                rs.getTimestamp("last_reviewed_at").toInstant()
        );
    }
}
