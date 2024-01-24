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

import com.capitan.chatapp.services.FriendRequestService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/friend-requests")
public class FriendRequestController {

    private FriendRequestService friendRequestService;

    public FriendRequestController(FriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;

    }

    @GetMapping("get-requests")
    public ResponseEntity<?> getFriendRequests(HttpServletRequest request) {

        return friendRequestService.getFriendRequests(request);

    }

    @PostMapping("add")
    public ResponseEntity<String> addFriend(@RequestBody FriendRequestDto friendRequestDto,
            HttpServletRequest request) {
        return friendRequestService.sendFriendRequest(friendRequestDto, request);

    }

    @PatchMapping("confirm")
    public ResponseEntity<String> approveFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        return friendRequestService.confirmFriendRequest(friendRequestOpDto, request);
    }

    @DeleteMapping("cancel")
    public ResponseEntity<String> cancelFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        return friendRequestService.cancelFriendRequest(friendRequestOpDto, request);

    }

}
