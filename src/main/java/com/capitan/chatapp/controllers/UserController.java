package com.capitan.chatapp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capitan.chatapp.dto.UpdateProfileImgDto;
import com.capitan.chatapp.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")

public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String prefix,
            HttpServletRequest request) {
        return userService.searchUsersByNicknamePrefix(prefix, request);

    }

    @PutMapping("setting/update-profile")
    public ResponseEntity<?> updateProfileImg(@RequestBody UpdateProfileImgDto profileImgDto,
            HttpServletRequest request) {
        return userService.updateProfileImage(profileImgDto.getImagePath(), request);
    }

    @PutMapping("setting/update-first-login")
    public ResponseEntity<?> updateProfileFirstLogin(HttpServletRequest request) {
        return userService.updateProfileFirstLogin(request);
    }

}
