package com.titran.pingcortex.controllers;

import com.titran.pingcortex.dto.request.LoginRequest;
import com.titran.pingcortex.dto.request.RegisterRequest;
import com.titran.pingcortex.dto.response.LoginResult;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResult> loginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<LoginResult> refreshToken(@RequestBody String rawRefreshToken) {
        return ResponseEntity.ok(authService.refresh(rawRefreshToken));
    }

    @PostMapping("/auth/logout")
    public void logoutUser(@RequestBody String refreshToken) {
        authService.logout(refreshToken);
    }
}
