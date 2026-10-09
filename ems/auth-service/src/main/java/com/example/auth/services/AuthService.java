package com.example.auth.services;

import com.example.auth.dtos.LoginRequest;
import com.example.auth.dtos.LoginResponse;
import com.example.auth.dtos.RegisterRequest;
import com.example.auth.entities.Credential;
import com.example.auth.jwt.JwtUtil;
import com.example.auth.repositories.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(CredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder,
                       RestTemplate restTemplate,
                       JwtUtil jwtUtil) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        Credential user = credentialRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtUtil.generate(user.getUsername(), user.getRole(), user.getUserId());
        return new LoginResponse(token);
    }

    public void register(RegisterRequest request) {
        // evităm duplicate
        Optional<Credential> existing = credentialRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        // generăm ID unic
        UUID userId = UUID.randomUUID();

        Credential credential = new Credential();
        credential.setUserId(userId);
        credential.setUsername(request.getUsername());
        credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credential.setRole(request.getRole() != null ? request.getRole() : "CLIENT");
        credentialRepository.save(credential);

        // pregătim payload pentru user-service
        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("id", userId.toString());
        userPayload.put("name", request.getName());
        userPayload.put("address", request.getAddress());
        userPayload.put("age", request.getAge());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userPayload, headers);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    "http://user-service:8081/users", entity, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                // rollback dacă user-service eșuează
                credentialRepository.deleteById(credential.getId());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Failed to sync user with user-service: " + response.getStatusCode());
            }

        } catch (Exception e) {
            credentialRepository.deleteById(credential.getId());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "User-service unavailable or rejected data: " + e.getMessage());
        }
    }
}
