package com.capitan.chatapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capitan.chatapp.models.FriendRequest;
import com.capitan.chatapp.repository.FriendRequestRepository;

@Service
public class FriendRequestService {
    @Autowired
    private FriendRequestRepository friendRequestsRepository;

    public void saveFriendRequest(FriendRequest friendRequest) {
        friendRequestsRepository.save(friendRequest);
    }

    public void deleteFriendRequest(int friendRequestId) {
        friendRequestsRepository.deleteById(friendRequestId);

    }
}
