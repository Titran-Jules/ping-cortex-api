package com.titran.pingcortex.repository;

import com.titran.pingcortex.dto.request.ApiKeyStatus;
import com.titran.pingcortex.dto.request.UserUpdate;
import com.titran.pingcortex.dto.response.ApiKeyResponse;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.model.AiProvider;
import com.titran.pingcortex.model.User;
import com.titran.pingcortex.util.ManagedConnection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
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
            throw new DataAccessException("Failed to find all users", e);
        }
        return users;
    }

    public Optional<UserResponse> findMe(UUID id) {
        String sql = """
            SELECT id, email, name, level, alert_threshold, created_at
            FROM \"user\"
            WHERE id = ?;
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(userResponseMapper(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user by id", e);
        }
    }

    public Optional<User> findByIdWithCredentials(UUID id) {
        String sql = """
            SELECT id, email, name, password_hash, level, token_version, alert_threshold, created_at
            FROM "user"
            WHERE id = ?
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(userMapper(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user by id", e);
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
            throw new DataAccessException("Failed to find  user by email", e);
        }
    }

    public Optional<Integer> findTokenVersion(UUID id) {
        String sql = "SELECT token_version FROM \"user\" WHERE id = ?";
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt("token_version")) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find token version", e);
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
            throw new DataAccessException("Failed to update user profile", e);
        }
        return userResponse;
    }

    public List<ApiKeyResponse> findMyApiKeys(UUID id) {
        String sql = """
            SELECT id, provider, is_active, created_at
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
            throw new DataAccessException("Failed to find user's api keys", e);
        }
        return apiKeyResponses;
    }

    public ApiKeyResponse createApiKey(UUID userId, AiProvider provider, String encryptedKey) {
        String sql = """
            INSERT INTO user_api_key (id, user_id, provider, encrypted_key) VALUES (?, ?, ?, ?)
            RETURNING id, provider, is_active, created_at
        """;
        ApiKeyResponse apiKeyResponse = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql);
        ) {
            var apiKeyId = UUID.randomUUID();
            stmt.setObject(1, apiKeyId);
            stmt.setObject(2, userId);
            stmt.setString(3, provider.name());
            stmt.setString(4, encryptedKey);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    apiKeyResponse = apiKeyResponseMapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create api key", e);
        }
        return apiKeyResponse;
    }

    public Optional<ApiKeyResponse> updateApiKeyStatus(UUID userId, UUID apiKeyId, ApiKeyStatus status) {
        String sql1 = """
            UPDATE user_api_key
            SET is_active = ?
            WHERE id = ? AND user_id = ?
            RETURNING id, provider, is_active, created_at
        """;
        String sql2 = """
            UPDATE user_api_key
            SET is_active = FALSE
            WHERE id != ? AND user_id = ? AND is_active = TRUE
        """;
        try (ManagedConnection connection = ManagedConnection.open(dataSource)) {
            Connection conn = connection.get();
            conn.setAutoCommit(false);
            ApiKeyResponse apiKeyResponse = null;
            try {
                try (PreparedStatement stmtTarget = conn.prepareStatement(sql1)) {
                    stmtTarget.setBoolean(1, status.isActive());
                    stmtTarget.setObject(2, apiKeyId);
                    stmtTarget.setObject(3, userId);
                    try (ResultSet rs = stmtTarget.executeQuery()) {
                        if (rs.next()) {
                            apiKeyResponse = apiKeyResponseMapper(rs);
                        } else {
                            conn.rollback();
                            return Optional.empty();
                        }
                    }
                }
                if (status.isActive()) {
                    try (PreparedStatement stmtOthers = conn.prepareStatement(sql2)) {
                        stmtOthers.setObject(1, apiKeyId);
                        stmtOthers.setObject(2, userId);
                        stmtOthers.executeUpdate();
                    }
                }
                conn.commit();
                return Optional.of(apiKeyResponse);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update api key status", e);
        }
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
            throw new DataAccessException("Failed to  delete api key", e);
        }
    }

    private ApiKeyResponse apiKeyResponseMapper(ResultSet rs) throws SQLException {
        return new ApiKeyResponse(
                rs.getObject("id",  UUID.class),
                rs.getString("provider"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
    private UserResponse userResponseMapper(ResultSet rs) throws SQLException {
        return new UserResponse(
                rs.getObject("id", UUID.class),
                rs.getString("email"),
                rs.getString("name"),
                rs.getString("level"),
                rs.getObject("alert_threshold", Integer.class),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private User userMapper(ResultSet rs) throws SQLException {
        return new User(
                rs.getObject("id", UUID.class),
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
