package com.titran.pingcortex.repositories;

import com.titran.pingcortex.dto.request.ApiKeyRequest;
import com.titran.pingcortex.dto.request.UserUpdate;
import com.titran.pingcortex.dto.response.ApiKeyResponse;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.utils.ManagedConnection;
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
public class UserRepository {
    private DataSource dataSource;

    public List<UserResponse> findAll() {
        String sql = """
            SELECT id, email, name, level, alert_threshold, created_at
            FROM \"user\"
            ORDER BY created_at DESC;
        """;
        List<UserResponse> users = new ArrayList<>();
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
        ) {
            while (rs.next()) {
                users.add(userResponseMapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public Optional<UserResponse> findById(UUID id) {
        String sql = """
            SELECT id, email, name, level, alert_threshold, created_at
            FROM \"user\"
            WHERE id = ?;
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(userResponseMapper(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UserResponse updateProfile(UUID id, UserUpdate userUpdate) {
        String sql = """
            UPDATE \"user\"
                SET level = ?,
                alert_threshold = ?,
            WHERE id = ?
            RETURNING id, email, name, level, alert_threshold, created_at
        """;
        UserResponse userResponse = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            stmt.setString(1, userUpdate.level());
            stmt.setInt(2, userUpdate.alertThreshold());
            stmt.setString(3, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    userResponse = userResponseMapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userResponse;
    }

    public List<ApiKeyResponse> findMyApiKeys(UUID id) {
        String sql = """
            SELECT id, provider, created_at
            FROM user_api_key
            WHERE user_id = ?
            ORDER BY created_at DESC;
        """;
        List<ApiKeyResponse>  apiKeyResponses = new ArrayList<>();
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    apiKeyResponses.add(apiKeyResponseMapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return apiKeyResponses;
    }

    public ApiKeyResponse createApiKey(UUID userId, ApiKeyRequest apiKeyRequest) {
        String sql = """
            INSERT INTO user_api_key (id, provider, apiKey) VALUES (?, ?, ?)
            WHERE user_id = ?
            RETURNING id, provider, created_at
        """;
        ApiKeyResponse apiKeyResponse = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            var apiKeyId = UUID.randomUUID();
            stmt.setString(1, apiKeyId.toString());
            stmt.setString(2, apiKeyRequest.provider());
            stmt.setString(3, apiKeyRequest.apiKey());
            stmt.setString(4, userId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    apiKeyResponse = apiKeyResponseMapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return apiKeyResponse;
    }

    public void deleteApiKey(UUID userId, UUID apiKeyId) {
        String sql = """
            DELETE FROM user_api_key
            WHERE user_id = ? AND id = ?
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            stmt.setString(1, userId.toString());
            stmt.setString(2, apiKeyId.toString());

            stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ApiKeyResponse apiKeyResponseMapper(ResultSet rs) throws SQLException {
        return new ApiKeyResponse(
                UUID.fromString(rs.getString("id")),
                rs.getString("provider"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
    public UserResponse userResponseMapper(ResultSet rs) throws SQLException {
        return new UserResponse(
                UUID.fromString(rs.getString("id")),
                rs.getString("email"),
                rs.getString("name"),
                rs.getString("level"),
                rs.getInt("alert_threshold"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
