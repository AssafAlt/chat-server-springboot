package com.capitan.chatapp.controllers;

import org.springframework.stereotype.Controller;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.capitan.chatapp.dto.FriendDto;
import com.capitan.chatapp.models.ConnectedUser;
import com.capitan.chatapp.models.Message;
import com.capitan.chatapp.models.MessageType;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.FriendshipRepository;
import com.capitan.chatapp.repository.UserRepository;
import com.capitan.chatapp.security.JwtGenerator;
import com.capitan.chatapp.services.MemberStore;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ChatController {

    private final MemberStore memberStore;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private JwtGenerator jwtGenerator;
    private UserRepository userRepository;
    private FriendshipRepository friendshipRepository;

    public ChatController(MemberStore memberStore, SimpMessagingTemplate simpMessagingTemplate,
            JwtGenerator jwtGenerator, UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.memberStore = memberStore;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            // Retrieve user information from the WebSocket session attributes
            Optional<UserEntity> user = userRepository
                    .findByUsername(jwtGenerator.getUsernameFromJwt(headerAccessor.getFirstNativeHeader("token")));
            if (user.isPresent()) {
                UserEntity opUser = user.get();
                ConnectedUser newUser = new ConnectedUser(opUser.getId().toString(), null, opUser.getNickname());

                // Retrieve the connected friends of the user
                Optional<List<FriendDto>> friendsOptional = friendshipRepository.getFriends(opUser.getId());

                // Store the user and their connected friends in the member store
                memberStore.addMember(opUser.getUsername(), friendsOptional);

                // Send the updated members list to all users
                // sendMembersList();
                System.out.println(memberStore.getFriends(opUser.getUsername()));

                // Notify all users about the user joining
                Message newMessage = new Message(newUser, null, null, MessageType.JOIN, Instant.now());
                simpMessagingTemplate.convertAndSend("/topic/messages", newMessage);
            }

        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    /*
     * @MessageMapping("/user")
     * public void getusers(ConnectedUser user, SimpMessageHeaderAccessor
     * headerAccessor) throws Exception {
     * ConnectedUser newUser = new ConnectedUser(user.id(), null, user.username());
     * headerAccessor.getSessionAttributes().put("user", newUser);
     * memberStore.addMember(newUser);
     * sendMembersList();
     * Message newMessage = new Message(new ConnectedUser(null, null,
     * user.username()), null, null, MessageType.JOIN,
     * Instant.now());
     * simpMessagingTemplate.convertAndSend("/topic/messages", newMessage);
     * 
     * }
     */

    /*
     * @EventListener
     * public void handleSessionConnectEvent(SessionConnectEvent event) {
     * try {
     * StompHeaderAccessor headerAccessor =
     * StompHeaderAccessor.wrap(event.getMessage());
     * // Retrieve user information from the WebSocket session attributes
     * Optional<UserEntity> user = userRepository
     * .findByUsername(jwtGenerator.getUsernameFromJwt(headerAccessor.
     * getFirstNativeHeader("token")));
     * if (user.isPresent()) {
     * UserEntity opUser = user.get();
     * ConnectedUser newUser = new ConnectedUser(opUser.getId().toString(), null,
     * opUser.getNickname());
     * // Store the user in the member store
     * memberStore.addMember(newUser);
     * // Send the updated members list to all users
     * // sendMembersList();
     * System.out.println(memberStore.getMembers());
     * Message newMessage = new Message(newUser, null, null, MessageType.JOIN,
     * Instant.now());
     * simpMessagingTemplate.convertAndSend("/topic/messages", newMessage);
     * }
     * 
     * } catch (Exception e) {
     * // TODO: handle exception
     * }
     * 
     * }
     */

    /*
     * @EventListener
     * public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
     * StompHeaderAccessor headerAccessor =
     * StompHeaderAccessor.wrap(event.getMessage());
     * String userId = headerAccessor.getFirstNativeHeader("user-id");
     * ConnectedUser disconnectedUser = memberStore.getMember(userId);
     * if (disconnectedUser != null) {
     * // Remove the disconnected user from the member store
     * memberStore.removeMember(disconnectedUser);
     * // Send the updated members list to all users
     * // sendMembersList();
     * // Notify all users about the user leaving
     * Message message = new Message(disconnectedUser, null, "", MessageType.LEAVE,
     * Instant.now());
     * simpMessagingTemplate.convertAndSend("/topic/messages", message);
     * }
     * }
     */

    /*
     * @MessageMapping("/message")
     * public void getMessage(Message message) throws Exception {
     * Message newMessage = new Message(new ConnectedUser(null,
     * message.user().serialId(), message.user().username()),
     * message.receiverId(), message.comment(), message.type(), Instant.now());
     * simpMessagingTemplate.convertAndSend("/topic/messages", newMessage);
     * }
     * 
     * @MessageMapping("/privatemessage")
     * public void getPrivateMessage(Message message) throws Exception {
     * Message newMessage = new Message(new ConnectedUser(null,
     * message.user().serialId(), message.user().username()),
     * message.receiverId(), message.comment(), message.type(), Instant.now());
     * simpMessagingTemplate.convertAndSendToUser(memberStore.getMember(message.
     * receiverId()).id(),
     * "/topic/privatemessages", newMessage);
     * 
     * }
     * 
     * private void sendMembersList() {
     * List<ConnectedUser> memberList = memberStore.getMembersList();
     * memberList.forEach(
     * sendUser -> simpMessagingTemplate.convertAndSendToUser(sendUser.id(),
     * "/topic/users",
     * memberStore.filterMemberListByUser(memberList, sendUser)));
     * }
     */
}
