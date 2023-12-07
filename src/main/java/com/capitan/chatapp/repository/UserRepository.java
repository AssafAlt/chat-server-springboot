package com.capitan.chatapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.capitan.chatapp.models.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u.profileImg FROM UserEntity u WHERE u.username = ?1")
    String findProfileImageByUsername(String username);

    Boolean existsByUsername(String username);

}
