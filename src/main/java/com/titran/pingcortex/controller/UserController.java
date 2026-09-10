package com.titran.pingcortex.controller;

import com.titran.pingcortex.dto.request.ApiKeyRequest;
import com.titran.pingcortex.dto.request.UserUpdate;
import com.titran.pingcortex.dto.response.ApiKeyResponse;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.security.CurrentUser;
import com.titran.pingcortex.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> findMe(HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        Optional<UserResponse> user = userService.findMe(userId);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/users/me")
    public ResponseEntity<UserResponse> updateMe(HttpServletRequest request, @RequestBody UserUpdate userUpdate) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.ok(userService.updateProfile(userId, userUpdate));
    }

    @GetMapping("/users/me/api-keys")
    public ResponseEntity<List<ApiKeyResponse>> findApiKeys(HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.ok(userService.findMyApiKeys(userId));
    }

    @PostMapping("/users/me/api-keys")
    public ResponseEntity<ApiKeyResponse> createApiKey(HttpServletRequest request, @RequestBody ApiKeyRequest apiKeyRequest) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createApiKey(userId, apiKeyRequest));
    }

    @DeleteMapping("/users/me/api-keys/{apiKeyId}")
    public ResponseEntity<Void> deleteApiKey(HttpServletRequest request, @PathVariable UUID apiKeyId) {
        UUID userId = CurrentUser.id(request);
        userService.deleteApiKey(userId, apiKeyId);
        return ResponseEntity.noContent().build();
    }
}
