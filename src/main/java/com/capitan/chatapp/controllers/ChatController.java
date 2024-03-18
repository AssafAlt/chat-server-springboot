package com.capitan.chatapp.controllers;

import org.springframework.stereotype.Controller;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.capitan.chatapp.models.ConnectedUser;
import com.capitan.chatapp.models.Message;
import com.capitan.chatapp.models.MessageType;
import com.capitan.chatapp.services.MemberStore;

@Controller
public class ChatController {

    private final MemberStore memberStore;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatController(MemberStore memberStore, SimpMessagingTemplate simpMessagingTemplate) {
        this.memberStore = memberStore;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/user")
    public void getusers(ConnectedUser user, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        ConnectedUser newUser = new ConnectedUser(user.id(), null, user.username());
        headerAccessor.getSessionAttributes().put("user", newUser);
        memberStore.addMember(newUser);
        sendMembersList();
        Message newMessage = new Message(new ConnectedUser(null, null, user.username()), null, null, MessageType.JOIN,
                Instant.now());
        simpMessagingTemplate.convertAndSend("/topic/messages", newMessage);

    }

    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        System.out.println("Session Connect Event");
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        System.out.println("Session Disconnect Event");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }
        ConnectedUser user = (ConnectedUser) sessionAttributes.get("user");
        if (user == null) {
            return;
        }
        memberStore.removeMember(user);
        sendMembersList();

        Message message = new Message(new ConnectedUser(null, null, user.username()), null, "", MessageType.LEAVE,
                Instant.now());
        simpMessagingTemplate.convertAndSend("/topic/messages", message);

    }

    @MessageMapping("/message")
    public void getMessage(Message message) throws Exception {
        Message newMessage = new Message(new ConnectedUser(null, message.user().serialId(), message.user().username()),
                message.receiverId(), message.comment(), message.type(), Instant.now());
        simpMessagingTemplate.convertAndSend("/topic/messages", newMessage);
    }

    @MessageMapping("/privatemessage")
    public void getPrivateMessage(Message message) throws Exception {
        Message newMessage = new Message(new ConnectedUser(null, message.user().serialId(), message.user().username()),
                message.receiverId(), message.comment(), message.type(), Instant.now());
        simpMessagingTemplate.convertAndSendToUser(memberStore.getMember(message.receiverId()).id(),
                "/topic/privatemessages", newMessage);

    }

    private void sendMembersList() {
        List<ConnectedUser> memberList = memberStore.getMembersList();
        memberList.forEach(
                sendUser -> simpMessagingTemplate.convertAndSendToUser(sendUser.id(), "/topic/users",
                        memberStore.filterMemberListByUser(memberList, sendUser)));
    }
}
