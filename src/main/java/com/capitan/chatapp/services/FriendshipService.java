package com.capitan.chatapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capitan.chatapp.models.Friendship;
import com.capitan.chatapp.repository.FriendshipRepository;

@Service
public class FriendshipService {
    @Autowired
    FriendshipRepository friendshipRepository;

    public void saveFriendship(Friendship friendship) {
        friendshipRepository.save(friendship);
    }
}
