package com.example.auth.entities;


import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Entity
public class Credential implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @UuidGenerator @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String role;

    public Credential() {}
    public Credential(String username, String passwordHash, UUID userId, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.userId = userId;
        this.role = role;
    }

    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public UUID getUserId() { return userId; } public void setUserId(UUID userId) { this.userId = userId; }
    public String getRole() { return role; } public void setRole(String role) { this.role = role; }
}
