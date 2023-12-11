package com.capitan.chatapp.controllers;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitan.chatapp.dto.FriendRequestDto;
import com.capitan.chatapp.dto.FriendRequestOpDto;
import com.capitan.chatapp.models.FriendRequest;
import com.capitan.chatapp.models.Friendship;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.security.JwtGenerator;
import com.capitan.chatapp.services.FriendRequestService;
import com.capitan.chatapp.services.FriendshipService;
import com.capitan.chatapp.services.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/friend-requests")
public class FriendRequestController {

    private FriendRequestService friendRequestService;

    private UserService userService;

    private FriendshipService friendshipService;

    private JwtGenerator jwtGenerator;

    public FriendRequestController(FriendRequestService friendRequestService, UserService userService,
            FriendshipService friendshipService,
            JwtGenerator jwtGenerator) {
        this.friendRequestService = friendRequestService;
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
        this.friendshipService = friendshipService;
    }

    @PostMapping("add")
    public ResponseEntity<String> addFriend(@RequestBody FriendRequestDto friendRequestDto,
            HttpServletRequest request) {
        try {
            Optional<UserEntity> sender = userService
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            Optional<UserEntity> reciever = userService.findByNickname(friendRequestDto.getUserNickname());

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
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("confirm")
    public ResponseEntity<String> approveFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        try {
            Optional<UserEntity> op = userService
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            Optional<UserEntity> sender = userService.findByNickname(friendRequestOpDto.getUserNickname());

            if (op.isPresent() && sender.isPresent()) {
                UserEntity opUser = op.get();
                UserEntity senderUser = sender.get();

                int friendRequestIdToApprove = friendRequestOpDto.getFriendRequestId();

                // Create a Friendship entity
                Friendship friendship = new Friendship();
                friendship.setUserOneNickname(senderUser.getNickname());
                friendship.setUserTwoNickname(opUser.getNickname());
                friendship.setUser1(senderUser);
                friendship.setUser2(opUser);
                friendship.setDate(LocalDate.now());

                // Save the Friendship entity
                friendshipService.saveFriendship(friendship);

                // Delete the FriendRequest Entity and handle user collections in the controller
                friendRequestService.deleteFriendRequest(friendRequestIdToApprove);

                senderUser.getSentFriendRequests()
                        .removeIf(sentRequest -> sentRequest.getId() == friendRequestIdToApprove);
                opUser.getSentFriendRequests().removeIf(sentRequest -> sentRequest.getId() == friendRequestIdToApprove);

                return new ResponseEntity<>("Friendship approved successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Friendship approval was failed", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @DeleteMapping("cancel")
    public ResponseEntity<String> cancelFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        try {
            Optional<UserEntity> op = userService
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            Optional<UserEntity> sender = userService.findByNickname(friendRequestOpDto.getUserNickname());

            if (op.isPresent() && sender.isPresent()) {
                UserEntity opUser = op.get();
                UserEntity senderUser = sender.get();

                // Assuming friendRequestDto.getFriendRequestId() returns the ID of the friend

                int friendRequestIdToDelete = friendRequestOpDto.getFriendRequestId();

                // Delete the FriendRequest Entity and handle user collections in the controller
                friendRequestService.deleteFriendRequest(friendRequestIdToDelete);

                senderUser.getSentFriendRequests()
                        .removeIf(sentRequest -> sentRequest.getId() == friendRequestIdToDelete);
                opUser.getSentFriendRequests().removeIf(sentRequest -> sentRequest.getId() == friendRequestIdToDelete);

                return new ResponseEntity<>("Request was deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Request deleting was failed!", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
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
