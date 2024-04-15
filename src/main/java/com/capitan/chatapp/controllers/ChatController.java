package com.capitan.chatapp.controllers;

import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.capitan.chatapp.dto.ChatMessageDto;
import com.capitan.chatapp.dto.ChatMessageResponseDto;
import com.capitan.chatapp.dto.FriendDto;
import com.capitan.chatapp.dto.FriendUpdateDto;
import com.capitan.chatapp.dto.FriendUpdateDto.MessageType;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.FriendshipRepository;
import com.capitan.chatapp.repository.UserRepository;
import com.capitan.chatapp.services.ChatService;

@Controller
public class ChatController {

    private ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private UserRepository userRepository;
    private FriendshipRepository friendshipRepository;

    public ChatController(ChatService chatService, SimpMessagingTemplate simpMessagingTemplate,
            UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.chatService = chatService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @GetMapping("/api/messages/{roomName}")
    public ResponseEntity<?> getMessagesHistoryByPaginating(
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @PathVariable String roomName) {
        return chatService.getMessagesHistoryByPaginating(roomName, pageNumber);
    }

    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        try {

            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String username = headerAccessor.getUser().getName();
            Optional<UserEntity> user = userRepository
                    .findByUsername(username);
            if (user.isPresent()) {
                UserEntity opUser = user.get();
                userRepository.updateOnlineStatus(username, true);
                String nickname = opUser.getNickname();
                headerAccessor.setSessionId(nickname);

                // Retrieve the user's online friends
                Optional<List<FriendDto>> friendsOptional = friendshipRepository.getOnlineFriends(opUser.getId());

                friendsOptional.ifPresent(friends -> {
                    List<String> onlineFriendNames = friends.stream()
                            .map(FriendDto::getNickname)
                            .collect(Collectors.toList());

                    // Notify each friend about the user's online status
                    onlineFriendNames.forEach(name -> {
                        FriendUpdateDto messagePayload = new FriendUpdateDto(nickname,
                                MessageType.JOIN);

                        simpMessagingTemplate.convertAndSendToUser(name, "/queue/onlineFriends", messagePayload);
                    });

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String username = headerAccessor.getUser().getName();
            Optional<UserEntity> user = userRepository
                    .findByUsername(username);
            if (user.isPresent()) {
                UserEntity opUser = user.get();
                userRepository.updateOnlineStatus(username, true);
                String nickname = opUser.getNickname();
                headerAccessor.setSessionId(nickname);

                Optional<List<FriendDto>> friendsOptional = friendshipRepository.getOnlineFriends(opUser.getId());

                friendsOptional.ifPresent(friends -> {
                    List<String> onlineFriendNames = friends.stream()
                            .map(FriendDto::getNickname)
                            .collect(Collectors.toList());

                    // Notify each friend about the user's online status
                    onlineFriendNames.forEach(name -> {
                        FriendUpdateDto messagePayload = new FriendUpdateDto(nickname,
                                MessageType.LEAVE);

                        simpMessagingTemplate.convertAndSendToUser(name, "/queue/onlineFriends", messagePayload);
                    });

                });
                userRepository.updateOnlineStatus(username, false);
                System.out.println("Disconnected: " + username);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @MessageMapping("/private.{roomName}")
    public void sendPrivateMessage(@DestinationVariable String roomName, ChatMessageDto message) {
        try {
            chatService.saveMessage(message);

            ChatMessageResponseDto newMessage = new ChatMessageResponseDto(message.getSender(), message.getContent(),
                    message.getDate(),
                    message.getTime());
            simpMessagingTemplate.convertAndSend("/topic/private." + roomName, newMessage);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
