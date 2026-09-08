package com.titran.pingcortex.repositories;

import com.titran.pingcortex.dto.request.RegisterRequest;
import com.titran.pingcortex.dto.response.UserResponse;
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

    public UserResponse register(RegisterRequest registerRequest) {
        String sql = """
            INSERT INTO \"user\" (id, email, name, password_hash, level)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, email, name, level, alertThreshold, created_at
        """;
        UserResponse userResponse = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            var userId = UUID.randomUUID();
            stmt.setString(1, userId.toString());
            stmt.setString(2, registerRequest.email());
            stmt.setString(3, registerRequest.name());
            stmt.setString(4, registerRequest.password());
            stmt.setString(5, registerRequest.level());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    userResponse = new UserResponse(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("email"),
                            rs.getString("name"),
                            rs.getString("level"),
                            rs.getInt("alert_threshold"),
                            rs.getTimestamp("created_at").toInstant()                    )
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userResponse;
    }
}
