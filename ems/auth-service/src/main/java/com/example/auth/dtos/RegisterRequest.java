package com.example.auth.dtos;

import java.util.UUID;

public class RegisterRequest {
    private String username;
    private String password;
    private String role; // "ADMIN" sau "CLIENT"
    private String name; // numele real al utilizatorului
    private String address; // orașul sau adresa completă
    private int age;

    // getter/setteri
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
