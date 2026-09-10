package com.titran.pingcortex.controllers;

import com.titran.pingcortex.dto.request.LoginRequest;
import com.titran.pingcortex.dto.request.RefreshRequest;
import com.titran.pingcortex.dto.request.RegisterRequest;
import com.titran.pingcortex.dto.response.LoginResult;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.securities.CurrentUser;
import com.titran.pingcortex.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResult> loginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<LoginResult> refreshToken(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logoutUser(@RequestBody RefreshRequest refreshRequest, HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        authService.logout(userId, refreshRequest.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
