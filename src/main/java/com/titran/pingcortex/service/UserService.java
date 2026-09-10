package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.request.ApiKeyRequest;
import com.titran.pingcortex.dto.request.UserUpdate;
import com.titran.pingcortex.dto.response.ApiKeyResponse;
import com.titran.pingcortex.dto.response.UserResponse;
import com.titran.pingcortex.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public List<UserResponse> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserResponse> findMe(UUID id) {
        return userRepository.findMe(id);
    }

    public UserResponse updateProfile(UUID id, UserUpdate userUpdate) {
        return userRepository.updateProfile(id, userUpdate);
    }

    public List<ApiKeyResponse> findMyApiKeys(UUID id) {
        return userRepository.findMyApiKeys(id);
    }

    public ApiKeyResponse createApiKey(UUID id, ApiKeyRequest apiKeyRequest) {
        String encryptedApiKey = encryptionService.encrypt(apiKeyRequest.apiKey());
        return userRepository.createApiKey(id, apiKeyRequest.provider(), encryptedApiKey);
    }

    public void deleteApiKey(UUID id, UUID apiKeyId) {
        userRepository.deleteApiKey(id, apiKeyId);
    }
}
