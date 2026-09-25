package com.titran.pingcortex.repository;

import com.titran.pingcortex.dto.response.ChatSessionResponse;
import com.titran.pingcortex.exception.DataAccessException;
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
}
