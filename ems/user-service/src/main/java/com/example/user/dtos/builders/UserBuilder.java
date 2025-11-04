package com.example.user.dtos.builders;

import com.example.user.dtos.UserDTO;
import com.example.user.dtos.UserDetailsDTO;
import com.example.user.entities.User;

public class UserBuilder {

    private UserBuilder() {
    }

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getAge());
    }

    public static UserDetailsDTO toUserDetailsDTO(User user) {
        return new UserDetailsDTO(user.getId(), user.getName(), user.getAddress(), user.getAge());
    }

    public static User toEntity(UserDetailsDTO userDetailsDTO) {
        User user = new User(
                userDetailsDTO.getName(),
                userDetailsDTO.getAddress(),
                userDetailsDTO.getAge()
        );
        user.setId(userDetailsDTO.getId()); // ← ACEASTA LINIE FIXEAZĂ TOTUL
        return user;
    }

}
