package com.titran.pingcortex.controllers;

import com.titran.pingcortex.dto.request.UserUpdate;
import com.titran.pingcortex.dto.response.ApiKeyResponse;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.securities.CurrentUser;
import com.titran.pingcortex.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
