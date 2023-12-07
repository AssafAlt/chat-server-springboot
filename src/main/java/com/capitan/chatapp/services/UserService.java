package com.capitan.chatapp.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public String findProfileImageByUsername(String username) {
        return userRepository.findProfileImageByUsername(username);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }
}