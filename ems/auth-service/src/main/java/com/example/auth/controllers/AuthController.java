package com.example.auth.controllers;

import com.example.auth.dtos.LoginRequest;
import com.example.auth.dtos.LoginResponse;
import com.example.auth.dtos.RegisterRequest;
import com.example.auth.services.AuthService;
import com.example.auth.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;
    private final JwtUtil jwt;

    public AuthController(AuthService service, JwtUtil jwt) {
        this.service = service;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest req) {
        service.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return service.login(req);
    }

    // === Traefik forwardAuth target ===
    // Valideaza JWT-ul si intoarce headerele pentru servicii.
    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractBearer(authorization);

        Jws<Claims> jws;
        try {
            jws = jwt.parse(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        Claims claims = jws.getBody();
        String userId = claims.get("userId", String.class);
        String role   = claims.get("role", String.class);

        return ResponseEntity.ok()
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-User-Role", role != null ? role : "USER")
                .build();
    }

    private String extractBearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }
        return header.substring("Bearer ".length());
    }
}
