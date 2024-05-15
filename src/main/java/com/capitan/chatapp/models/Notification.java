package com.capitan.chatapp.models;

import com.capitan.chatapp.dto.FriendIsOnlineDto;
import com.capitan.chatapp.dto.GetFriendRequestDto;

import lombok.Data;

@Data
public class Notification {
    private String message;
    private MessageType messageType;
    private FriendIsOnlineDto friend;
    private GetFriendRequestDto fRequest;

    public Notification(String message, MessageType messageType, FriendIsOnlineDto friend) {
        this.message = message;
        this.messageType = messageType;
        this.friend = friend;
    }

    public Notification(String message, MessageType messageType, GetFriendRequestDto fRequest) {
        this.message = message;
        this.messageType = messageType;
        this.fRequest = fRequest;
    }

}
