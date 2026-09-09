package com.titran.pingcortex.services;

import com.titran.pingcortex.dto.request.LoginRequest;
import com.titran.pingcortex.dto.request.RegisterRequest;
import com.titran.pingcortex.dto.response.LoginResult;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.exceptions.InvalidCredentialsException;
import com.titran.pingcortex.models.User;
import com.titran.pingcortex.repositories.AuthRepository;
import com.titran.pingcortex.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserResponse register(RegisterRequest request) {
        String passwordHash = passwordService.hash(request.password());
        return authRepository.register(
                request.email(),
                request.name(),
                passwordHash,
                request.level()
        );
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).
                orElseThrow(InvalidCredentialsException::new);
        if (!passwordService.matches(request.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user.id());
        return new LoginResult(accessToken, refreshToken);
    }

    public LoginResult refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.validateAndRotate(rawRefreshToken);
        User user = userRepository.findByIdWithCredentials(rotation.userId()).
                orElseThrow(InvalidCredentialsException::new);
        String newAccessToken = jwtService.generateAccessToken(user);
        return new LoginResult(newAccessToken, rotation.newRawToken());
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }
}
