package com.example.user.controllers;

import com.example.user.dtos.UserDTO;
import com.example.user.dtos.UserDetailsDTO;
import com.example.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users - doar ADMIN
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok(userService.findUsers());
    }

    // GET /users/{id} - ADMIN sau propriul profil
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUserById(@PathVariable UUID id,
                                                      @RequestHeader("X-User-Id") String callerId,
                                                      @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !id.toString().equals(callerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok(userService.findUserById(id));
    }

    // POST /users - doar ADMIN
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody UserDetailsDTO user) {
        UUID id = userService.insert(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build(); // 201 + Location header
    }


    // PUT /users/{id} - ADMIN sau clientul pe sine
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id,
                                           @Valid @RequestBody UserDetailsDTO user,
                                           @RequestHeader("X-User-Id") String callerId,
                                           @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !id.toString().equals(callerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        userService.update(id, user);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // DELETE /users/{id} - doar ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id,
                                           @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete users");
        }

        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
