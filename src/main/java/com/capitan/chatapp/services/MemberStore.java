package com.capitan.chatapp.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedList;

import org.springframework.stereotype.Service;

import com.capitan.chatapp.models.ConnectedUser;

@Service
public class MemberStore {

    private static List<ConnectedUser> store = new LinkedList<>();

    public List<ConnectedUser> getMembersList() {
        AtomicInteger serialId = new AtomicInteger(1);
        return store.stream()
                .map(user -> new ConnectedUser(user.id(), serialId.getAndIncrement() + "", user.username()))
                .toList();
    }

    public List<ConnectedUser> filterMemberListByUser(List<ConnectedUser> memberList, ConnectedUser user) {
        return memberList.stream()
                .filter(filterUser -> filterUser.id() != user.id())
                .map(sendUser -> new ConnectedUser(null, sendUser.serialId(), sendUser.username()))
                .toList();
    }

    public ConnectedUser getMember(String id) {
        return store.get(Integer.valueOf(id) - 1);
    }

    public void addMember(ConnectedUser member) {
        store.add(member);
    }

    public void removeMember(ConnectedUser member) {
        store.remove(member);
    }
}
