package com.example.device.services;

import com.example.device.entities.SyncedUser;
import com.example.device.events.UserOperationEvent;
import com.example.device.events.UserIdEvent;
import com.example.device.repositories.SyncedUserRepository;
import org.springframework.stereotype.Service;

@Service
public class SyncedUserService {

    private final SyncedUserRepository repo;

    public SyncedUserService(SyncedUserRepository repo) {
        this.repo = repo;
    }

    public void createUser(UserOperationEvent dto) {
        repo.save(new SyncedUser(
                dto.id(),
                dto.name(),
                dto.address(),
                dto.age()
        ));
    }

    public void deleteUser(UserIdEvent dto) {
        repo.deleteById(dto.userId());
    }

    public void updateUser(UserOperationEvent dto) {
        repo.findById(dto.id()).ifPresent(user -> {
            user.setName(dto.name());
            user.setAddress(dto.address());
            user.setAge(dto.age());
            repo.save(user);
        });
    }

}
