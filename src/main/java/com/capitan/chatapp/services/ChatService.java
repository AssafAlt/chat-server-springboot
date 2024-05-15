package com.capitan.chatapp.services;

import java.util.Optional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
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
import com.capitan.chatapp.dto.MessagesHistoryResponseDto;

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
            int pageSize = 10;
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<ChatMessageResponseDto> messagesPage = conversationRepository
                    .findLastTenMessagesByConversationRoomName(roomName, pageable);

            if (messagesPage.isEmpty()) {
                return new ResponseEntity<>("No messages history", HttpStatus.NOT_FOUND);
            } else {
                int totalPages = messagesPage.getTotalPages();
                int currentPage = messagesPage.getNumber();
                Boolean hasNext = messagesPage.hasNext();
                List<ChatMessageResponseDto> messages = messagesPage.getContent();
                Map<String, List<ChatMessageResponseDto>> messagesByDate = new LinkedHashMap<>();

                // Group messages by date
                for (ChatMessageResponseDto message : messages) {
                    String date = message.getDate();
                    messagesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(message);
                }

                MessagesHistoryResponseDto messagesHistoryResponse = new MessagesHistoryResponseDto(totalPages,
                        currentPage, messagesByDate, hasNext);

                return new ResponseEntity<>(messagesHistoryResponse, HttpStatus.OK);

            }

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

}
