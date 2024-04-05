package com.capitan.chatapp.services;

import java.util.Optional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.capitan.chatapp.dto.ChatMessageDto;
import com.capitan.chatapp.models.ChatMessage;
import com.capitan.chatapp.models.Conversation;
import com.capitan.chatapp.repository.ChatRepository;
import com.capitan.chatapp.repository.ConversationRepository;
import com.capitan.chatapp.dto.ChatMessageResponseDto;

@Service
public class ChatService {

    private ConversationRepository conversationRepository;
    private ChatRepository chatRepository;

    public ChatService(ConversationRepository conversationRepository,
            ChatRepository chatRepository) {
        this.conversationRepository = conversationRepository;
        this.chatRepository = chatRepository;
    }

    public void saveMessage(ChatMessageDto chatMessageDto) {
        try {
            Optional<Conversation> optionalConversation = conversationRepository.findById(chatMessageDto.getRoom());

            if (optionalConversation.isPresent()) {
                Conversation conversation = optionalConversation.get();
                ChatMessage newChatMessage = new ChatMessage(conversation, chatMessageDto.getSender(),
                        chatMessageDto.getRecipient(), chatMessageDto.getContent(), chatMessageDto.getDate(),
                        chatMessageDto.getTime());
                chatRepository.save(newChatMessage);
            } else {
                Conversation newConversation = new Conversation(chatMessageDto.getRoom());
                conversationRepository.save(newConversation);
                ChatMessage newChatMessage = new ChatMessage(newConversation, chatMessageDto.getSender(),
                        chatMessageDto.getRecipient(), chatMessageDto.getContent(), chatMessageDto.getDate(),
                        chatMessageDto.getTime());
                chatRepository.save(newChatMessage);

            }
        } catch (Exception e) {
            System.out.println(e);

        }

    }

    public ResponseEntity<?> getMessagesHistoryByPaginating(String roomName, int pageNumber) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, 10); //
            Optional<List<ChatMessageResponseDto>> optionalMessages = conversationRepository
                    .findLastTenMessagesByConversationRoomName(roomName, pageable);
            if (!optionalMessages.isPresent()) {
                return new ResponseEntity<>("No messages history", HttpStatus.NOT_FOUND);
            } else {
                List<ChatMessageResponseDto> messages = optionalMessages.get();
                Map<String, List<ChatMessageResponseDto>> messagesByDate = new LinkedHashMap<>();

                // Group messages by date
                for (ChatMessageResponseDto message : messages) {
                    String date = message.getDate();
                    messagesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(message);
                }
                if (messages.size() < 10) {
                    return new ResponseEntity<>(messagesByDate, HttpStatus.PARTIAL_CONTENT);
                } else {
                    return new ResponseEntity<>(messagesByDate, HttpStatus.OK);
                }
            }

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

}
