package com.capitan.chatapp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitan.chatapp.dto.SearchedUserResponseDto;
import com.capitan.chatapp.services.UserService;

@RestController
@RequestMapping("/api/users")

public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search/{partialNickname}")
    public ResponseEntity<List<SearchedUserResponseDto>> searchUsersByNickname(@PathVariable String partialNickname) {
        return userService.searchUsersByNickname(partialNickname);
    }

}
