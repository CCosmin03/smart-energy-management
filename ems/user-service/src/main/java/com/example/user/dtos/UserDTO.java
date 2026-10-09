package com.example.user.dtos;

import java.util.Objects;
import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String name;
    private int age;

    public UserDTO() {}
    public UserDTO(UUID id, String name, int age) {
        this.id = id; this.name = name; this.age = age;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO that = (UserDTO) o;
        return age == that.age && Objects.equals(name, that.name);
    }
    @Override public int hashCode() { return Objects.hash(name, age); }
}
