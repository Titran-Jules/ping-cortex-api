package com.titran.pingcortex.repository;

import com.titran.pingcortex.dto.response.ChatMessagesResponse;
import com.titran.pingcortex.dto.response.ChatSessionResponse;
import com.titran.pingcortex.exception.DataAccessException;
import com.titran.pingcortex.model.Role;
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
public class ChatAiRepository {
    private final DataSource dataSource;

    public ChatSessionResponse createSession(UUID userId, UUID courseId) {
        String sql = """
            INSERT INTO chat_session (id, user_id, course_id) VALUES (?, ?, ?)
            RETURNING id,  user_id, course_id, created_at
        """;
        ChatSessionResponse response = null;
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
             PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            UUID sessionId = UUID.randomUUID();
            stmt.setObject(1, sessionId);
            stmt.setObject(2, userId);
            stmt.setObject(3, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    response = new ChatSessionResponse(
                            rs.getObject("id", UUID.class),
                            rs.getObject("course_id", UUID.class),
                            rs.getTimestamp("created_at").toInstant()
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert a new chat session", e);
        }
        return response;
    }

    public List<ChatMessagesResponse> findAllMessagesByCourseId(UUID userId, UUID sessionId) {
        String sql = """
            SELECT m.id, m.role, m.content, m.created_at
            FROM chat_message m
            JOIN chat_session s ON m.session_id = s.id
            WHERE s.user_id = ? AND m.session_id = ?
        """;
        List<ChatMessagesResponse> messages = new ArrayList<>();
        try (ManagedConnection connection = ManagedConnection.open(dataSource);
            PreparedStatement stmt = connection.get().prepareStatement(sql)
        ) {
            stmt.setObject(1, userId);
            stmt.setObject(2, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(messageRowMapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find all messages of this course", e);
        }
        return messages;
    }

    private ChatMessagesResponse messageRowMapper(ResultSet rs) throws SQLException {
        return new ChatMessagesResponse(
                rs.getObject("id", UUID.class),
                Role.valueOf(rs.getString("role")),
                rs.getString("content"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
