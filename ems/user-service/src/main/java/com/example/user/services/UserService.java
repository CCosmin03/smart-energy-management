package com.example.user.services;

import com.example.user.dtos.UserDTO;
import com.example.user.dtos.UserDetailsDTO;
import com.example.user.dtos.builders.UserBuilder;
import com.example.user.entities.User;
import com.example.user.repositories.UserRepository;
import com.example.user.handlers.exceptions.model.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.user.events.SyncEvent;
import com.example.user.events.UserOperationEvent;
import com.example.user.events.UserIdEvent;
import com.example.user.config.RabbitConfig;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    public UserService(UserRepository userRepository,
                       RabbitTemplate rabbitTemplate,
                       ObjectMapper mapper) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
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

        try {
            UserOperationEvent dto = new UserOperationEvent(
                    user.getId(),
                    user.getName(),
                    user.getAddress(),
                    user.getAge()
            );

            String payload = mapper.writeValueAsString(dto);

            SyncEvent event = new SyncEvent("USER_CREATED", payload);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    event
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish USER_CREATED event", e);
        }

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

        user = userRepository.save(user);
        LOGGER.debug("User with id {} was updated", id);

        try {
            UserOperationEvent event = new UserOperationEvent(
                    user.getId(),
                    user.getName(),
                    user.getAddress(),
                    user.getAge()
            );

            SyncEvent sync = new SyncEvent(
                    "USER_UPDATED",
                    mapper.writeValueAsString(event)
            );

            rabbitTemplate.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    sync
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish USER_UPDATED event", e);
        }
    }


    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            LOGGER.error("User with id {} not found for deletion", id);
            throw new ResourceNotFoundException("User with id: " + id);
        }

        userRepository.deleteById(id);
        LOGGER.debug("User with id {} was deleted", id);

        try {
            UserIdEvent dto = new UserIdEvent(id);

            String payload = mapper.writeValueAsString(dto);

            SyncEvent event = new SyncEvent("USER_DELETED", payload);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    event
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish USER_DELETED event", e);
        }
    }
}
