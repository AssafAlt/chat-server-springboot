package com.capitan.chatapp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitan.chatapp.dto.FriendRequestDto;
import com.capitan.chatapp.dto.FriendRequestOpDto;

import com.capitan.chatapp.services.FriendshipService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/friendship")
public class FriendshipController {

    private FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;

    }

    @GetMapping("get-requests")
    public ResponseEntity<?> getFriendRequests(HttpServletRequest request) {

        return friendshipService.getFriendRequests(request);

    }

    @PostMapping("add")
    public ResponseEntity<String> addFriend(@RequestBody FriendRequestDto friendRequestDto,
            HttpServletRequest request) {
        return friendshipService.sendFriendRequest(friendRequestDto, request);

    }

    @PatchMapping("confirm")
    public ResponseEntity<String> approveFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        return friendshipService.confirmFriendRequest(friendRequestOpDto, request);
    }

    @DeleteMapping("cancel")
    public ResponseEntity<String> cancelFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        return friendshipService.cancelFriendRequest(friendRequestOpDto, request);

    }

    @DeleteMapping("delete")
    public ResponseEntity<String> deleteFriendship(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        return friendshipService.deleteFriendship(friendRequestOpDto, request);

    }

}
