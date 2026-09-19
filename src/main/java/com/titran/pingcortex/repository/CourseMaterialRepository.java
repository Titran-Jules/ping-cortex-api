package com.titran.pingcortex.repository;

import com.titran.pingcortex.dto.response.CourseMaterialResponse;
import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.model.MaterialType;
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
public class CourseMaterialRepository {
    private DataSource dataSource;

    public CourseMaterialResponse create (UUID courseId, String content, MaterialType type) {
        String sql = """
            INSERT INTO course_material (id, course_id, content, type)
            VALUES (?, ?, ?, ?)
            RETURNING id, content, type, created_at;
        """;
        CourseMaterialResponse courseMaterialResponse = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            UUID courseMaterialId = UUID.randomUUID();
            stmt.setObject(1, courseMaterialId);
            stmt.setObject(2, courseId);
            stmt.setString(3, content);
            stmt.setString(4, type.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    courseMaterialResponse = courseMaterialRowMapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create course material", e);
        }
        return  courseMaterialResponse;
    }

    public List<CourseMaterialResponse> findAllByCourseId(UUID courseId) {
        String sql = """
            SELECT id, content, type, created_at
            FROM course_material
            WHERE course_id = ?;
        """;
        List<CourseMaterialResponse> courseMaterials = new ArrayList<>();
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courseMaterials.add(courseMaterialRowMapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find course materials", e);
        }
        return courseMaterials;
    }

    public Optional<CourseMaterialResponse> findById(UUID materialId) {
        String sql = """
            SELECT id, content, type, created_at
            FROM course_material
            WHERE id = ?;
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, materialId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(courseMaterialRowMapper(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find Material", e);
        }
    }

    private CourseMaterialResponse courseMaterialRowMapper(ResultSet rs) throws SQLException {
        return new CourseMaterialResponse(
                rs.getObject("id", UUID.class),
                rs.getString("content"),
                MaterialType.valueOf(rs.getString("type")),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
