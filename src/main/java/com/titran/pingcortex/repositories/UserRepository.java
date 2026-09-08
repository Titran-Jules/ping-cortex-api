package com.titran.pingcortex.repositories;

import com.titran.pingcortex.dto.request.ApiKeyRequest;
import com.titran.pingcortex.dto.request.UserUpdate;
import com.titran.pingcortex.dto.response.ApiKeyResponse;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.models.User;
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
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(userResponseMapper(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
            SELECT id, email, name, password_hash, level, token_version, alert_threshold, created_at
            FROM \"user\"
            WHERE email = ?;
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(userMapper(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UserResponse updateProfile(UUID id, UserUpdate userUpdate) {
        String sql = """
            UPDATE \"user\"
                SET level = ?,
                alert_threshold = ?
            WHERE id = ?
            RETURNING id, email, name, level, alert_threshold, created_at
        """;
        UserResponse userResponse = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            stmt.setString(1, userUpdate.level());
            stmt.setObject(2, userUpdate.alertThreshold());
            stmt.setObject(3, id);
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
            stmt.setObject(1, id);
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
            INSERT INTO user_api_key (id, user_id, provider, encrypted_key) VALUES (?, ?, ?, ?)
            RETURNING id, provider, created_at
        """;
        ApiKeyResponse apiKeyResponse = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            var apiKeyId = UUID.randomUUID();
            stmt.setObject(1, apiKeyId);
            stmt.setObject(2, userId);
            stmt.setString(3, apiKeyRequest.provider());
            stmt.setString(4, apiKeyRequest.encryptedKey());

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
            stmt.setObject(1, userId);
            stmt.setObject(2, apiKeyId);

            stmt.executeUpdate();
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
                rs.getObject("alert_threshold", Integer.class),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    public User userMapper(ResultSet rs) throws SQLException {
        return new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("name"),
                rs.getString("level"),
                rs.getInt("token_version"),
                rs.getObject("alert_threshold", Integer.class),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
