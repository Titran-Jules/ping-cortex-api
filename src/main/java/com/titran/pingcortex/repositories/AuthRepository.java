package com.titran.pingcortex.repositories;

import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.exceptions.DataAccessException;
import com.titran.pingcortex.utils.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class AuthRepository {
    private final DataSource dataSource;

    public UserResponse register(String email, String name, String passwordHash, String level) {
        String sql = """
            INSERT INTO "user" (id, email, name, password_hash, level)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, email, name, level, alert_threshold, created_at
        """;
        UserResponse userResponse = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            var userId = UUID.randomUUID();
            stmt.setObject(1,  userId);
            stmt.setString(2, email);
            stmt.setString(3, name);
            stmt.setString(4, passwordHash);
            stmt.setString(5, level);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    userResponse = new UserResponse(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("email"),
                            rs.getString("name"),
                            rs.getString("level"),
                            rs.getObject("alert_threshold", Integer.class),
                            rs.getTimestamp("created_at").toInstant()
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("User registration failed", e);
        }
        return userResponse;
    }
}
