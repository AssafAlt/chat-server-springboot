package com.capitan.chatapp.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.capitan.chatapp.dto.FriendDto;
import com.capitan.chatapp.models.ConnectedUser;

@Service
public class MemberStore {

    private static Map<String, Optional<List<FriendDto>>> store = new HashMap<>();

    public List<ConnectedUser> getMembersList() {
        AtomicInteger serialId = new AtomicInteger(1);
        return store.keySet().stream()
                .map(username -> new ConnectedUser(null, serialId.getAndIncrement() + "", username))
                .toList();
    }

    public ConnectedUser getMember(String username) {
        return new ConnectedUser(null, null, username);
    }

    public void addMember(String username, Optional<List<FriendDto>> friends) {
        if (friends != null && friends.isPresent()) {
            store.put(username, friends);
        } else {
            store.put(username, Optional.empty());
        }
    }

    public void removeMember(String username) {
        store.remove(username);
    }

    public Optional<List<FriendDto>> getFriends(String username) {
        return store.get(username);
    }

    public void updateConnectedFriends(String username, Optional<List<FriendDto>> friends) {
        store.put(username, friends);
    }
}
