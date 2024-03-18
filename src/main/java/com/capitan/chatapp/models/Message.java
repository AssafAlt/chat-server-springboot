package com.capitan.chatapp.models;

import java.time.Instant;

public record Message(ConnectedUser user, String receiverId, String comment, MessageType type, Instant timestamp) {

}
