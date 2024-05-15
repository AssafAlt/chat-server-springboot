package com.capitan.chatapp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capitan.chatapp.dto.UpdateProfileImgDto;
import com.capitan.chatapp.services.FriendshipService;
import com.capitan.chatapp.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserService userService;
    private FriendshipService friendshipService;

    public UserController(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
    }

    @DeleteMapping("delete-user")
    public ResponseEntity<?> deleteUserByUsernameAndPassword(HttpServletRequest request, String password) {
        return userService.deleteUserByUsernameAndPassword(request, password);
    }

    @GetMapping("search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String prefix,
            HttpServletRequest request) {
        return userService.searchUsersByNicknamePrefix(prefix, request);

    }

    @GetMapping("online-check")
    public ResponseEntity<?> checkIsFriendOnline(
            @RequestParam String nickname) {
        return userService.checkIsFriendOnlineByNickname(nickname);

    }

    @GetMapping("friends")
    public ResponseEntity<?> getFriends(HttpServletRequest request) {
        return friendshipService.getFriends(request);

    }

    @GetMapping("online-friends")
    public ResponseEntity<?> getOnlineFriends(HttpServletRequest request) {
        return friendshipService.getOnlineFriends(request);

    }

    @GetMapping("friends-status")
    public ResponseEntity<?> getFriendsStatus(HttpServletRequest request) {
        return friendshipService.getFriendsWithStatus(request);

    }

    @PatchMapping("setting/image")
    public ResponseEntity<?> updateProfileImg(@RequestBody UpdateProfileImgDto profileImgDto,
            HttpServletRequest request) {
        return userService.updateProfileImage(profileImgDto, request);
    }

    @GetMapping("setting/update-first-login")
    public ResponseEntity<?> updateProfileFirstLogin(HttpServletRequest request) {
        return userService.updateProfileFirstLogin(request);
    }

}
