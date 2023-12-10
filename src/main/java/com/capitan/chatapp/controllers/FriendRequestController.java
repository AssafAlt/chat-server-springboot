package com.capitan.chatapp.controllers;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitan.chatapp.dto.FriendRequestDto;
import com.capitan.chatapp.models.FriendRequest;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.security.JwtGenerator;
import com.capitan.chatapp.services.FriendRequestService;
import com.capitan.chatapp.services.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/friends")
public class FriendRequestController {

    private FriendRequestService friendRequestService;

    private UserService userService;

    private JwtGenerator jwtGenerator;

    public FriendRequestController(FriendRequestService friendRequestService, UserService userService,
            JwtGenerator jwtGenerator) {
        this.friendRequestService = friendRequestService;
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("add")
    public ResponseEntity<String> addFriend(@RequestBody FriendRequestDto friendRequestDto,
            HttpServletRequest request) {
        Optional<UserEntity> sender = userService
                .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
        Optional<UserEntity> reciever = userService.findByNickname(friendRequestDto.getReceiverNickname());

        if (sender.isPresent() && reciever.isPresent()) {
            UserEntity senderUser = sender.get();
            UserEntity recieverUser = reciever.get();

            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setReceiverNickname(recieverUser.getNickname());
            friendRequest.setSenderNickname(senderUser.getNickname());
            friendRequest.setDate(LocalDate.now());

            // Set the sender and receiver entities
            friendRequest.setSenderEntity(senderUser);
            friendRequest.setReceiverEntity(recieverUser);

            friendRequestService.saveFriendRequest(friendRequest);
            return new ResponseEntity<>("Request was sent successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Request was failed!", HttpStatus.BAD_REQUEST);
        }
    }

    private String getJWTFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
