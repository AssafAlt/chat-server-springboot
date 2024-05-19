package com.capitan.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitan.chatapp.models.ChatMessage;

import jakarta.transaction.Transactional;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    @Transactional
    default Long saveAndReturnId(ChatMessage chatMessage) {
        ChatMessage savedMessage = save(chatMessage);
        return savedMessage.getId();
    }

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage cm WHERE cm.id = :messageId")
    void deleteUserById(@Param("messageId") Long messageId);

}
