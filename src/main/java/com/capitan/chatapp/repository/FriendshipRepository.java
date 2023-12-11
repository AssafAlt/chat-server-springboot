package com.capitan.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capitan.chatapp.models.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

}
