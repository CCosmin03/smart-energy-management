package com.example.user.services;

import com.example.user.dtos.UserDTO;
import com.example.user.dtos.UserDetailsDTO;
import com.example.user.dtos.builders.UserBuilder;
import com.example.user.entities.User;
import com.example.user.repositories.UserRepository;
import com.example.user.handlers.exceptions.model.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> findUsers() {
        return userRepository.findAll().stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("User with id {} was not found", id);
                    return new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
                });

        return UserBuilder.toUserDetailsDTO(user);
    }

    public UUID insert(UserDetailsDTO userDTO) {
        User user = UserBuilder.toEntity(userDTO);
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was inserted", user.getId());
        return user.getId();
    }

    public void update(UUID id, UserDetailsDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("User with id {} not found for update", id);
                    return new ResourceNotFoundException("User with id: " + id);
                });

        user.setName(dto.getName());
        user.setAddress(dto.getAddress());
        user.setAge(dto.getAge());

        userRepository.save(user);
        LOGGER.debug("User with id {} was updated", id);
    }

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            LOGGER.error("User with id {} not found for deletion", id);
            throw new ResourceNotFoundException("User with id: " + id);
        }

        userRepository.deleteById(id);
        LOGGER.debug("User with id {} was deleted", id);
    }
}
