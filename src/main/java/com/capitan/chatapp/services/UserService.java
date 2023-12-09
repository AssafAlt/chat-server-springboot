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

    public String getProfileImageByUsername(String username) {
        return userRepository.getProfileImageByUsername(username);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<UserEntity> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public String getNicknameByUsername(String username) {
        return userRepository.getNicknameByUsername(username);
    }

    public Boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }
}