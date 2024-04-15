package com.capitan.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capitan.chatapp.models.ChatMessage;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

}
