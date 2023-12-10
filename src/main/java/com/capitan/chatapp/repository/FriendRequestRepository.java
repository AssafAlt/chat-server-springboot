package com.capitan.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capitan.chatapp.models.FriendRequest;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {

}
