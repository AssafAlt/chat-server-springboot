package com.capitan.chatapp.controllers;

import org.springframework.stereotype.Controller;

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
import com.capitan.chatapp.services.ChatService;

@Controller
public class ChatController {

    private ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatService = chatService;
        this.simpMessagingTemplate = simpMessagingTemplate;

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
            chatService.connectEvent(headerAccessor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            chatService.disconnectEvent(headerAccessor);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @MessageMapping("/private.{roomName}")
    public void sendPrivateMessage(@DestinationVariable String roomName, ChatMessageDto message) {
        try {
            Long messageId = chatService.saveMessage(message);

            ChatMessageResponseDto newMessage = new ChatMessageResponseDto(messageId, message.getSender(),
                    message.getContent(),
                    message.getDate(),
                    message.getTime());
            simpMessagingTemplate.convertAndSend("/topic/private." + roomName, newMessage);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    @MessageMapping("/delete.private.{roomName}")
    public void deletePrivateMessage(@DestinationVariable String roomName, String messageId) {
        try {
            Long messageIdToDelete = Long.valueOf(messageId);
            chatService.deleteChatMessageById(messageIdToDelete);
            simpMessagingTemplate.convertAndSend("/topic/delete.private." + roomName, messageIdToDelete);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
