package com.example.auth.dtos;

public class UserDetailsSyncDTO {
    private String username;
    private int age;

    public UserDetailsSyncDTO() {}

    public UserDetailsSyncDTO(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
