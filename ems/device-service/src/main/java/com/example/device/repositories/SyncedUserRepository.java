package com.example.device.repositories;

import com.example.device.entities.SyncedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SyncedUserRepository extends JpaRepository<SyncedUser, UUID> {}
