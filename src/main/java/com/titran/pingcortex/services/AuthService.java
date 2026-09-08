package com.titran.pingcortex.services;

import com.titran.pingcortex.dto.request.LoginRequest;
import com.titran.pingcortex.dto.request.RegisterRequest;
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

    public UserResponse register(RegisterRequest request) {
        String passwordHash = passwordService.hash(request.password());
        return authRepository.register(
                request.email(),
                request.name(),
                passwordHash,
                request.level()
        );
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordService.matches(request.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return jwtService.generateAccessToken(user);
    }
}
