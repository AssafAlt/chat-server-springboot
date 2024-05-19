package com.capitan.chatapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.capitan.chatapp.dto.ChatMessageResponseDto;
import com.capitan.chatapp.models.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT NEW com.capitan.chatapp.dto.ChatMessageResponseDto(cm.id,cm.sender, cm.content, cm.date, cm.time) FROM ChatMessage cm WHERE cm.conversation.roomName = :roomName ORDER BY cm.id DESC")
    Page<ChatMessageResponseDto> findLastTenMessagesByConversationRoomName(@Param("roomName") String roomName,
            Pageable pageable);
}
