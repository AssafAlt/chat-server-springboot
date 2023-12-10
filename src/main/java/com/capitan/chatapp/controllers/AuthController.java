package com.capitan.chatapp.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitan.chatapp.dto.AuthResponseDto;
import com.capitan.chatapp.dto.LoginDto;
import com.capitan.chatapp.dto.RegisterDto;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.RoleRepository;
import com.capitan.chatapp.security.JwtGenerator;
import com.capitan.chatapp.services.UserService;

import java.util.Collections;

import com.capitan.chatapp.models.Role;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private UserService userService;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtGenerator jwtGenerator;

    public AuthController(AuthenticationManager authenticationManager, UserService userService,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;

    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);
        String profileImagePath = userService.getProfileImageByUsername(loginDto.getUsername());
        return new ResponseEntity<>(new AuthResponseDto(token, profileImagePath), HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        if (userService.existsByUsername((registerDto.getUsername()))) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setProfileImg(registerDto.getProfileImg());
        user.setNickname(user.generateEncodedNickname(registerDto.getNickname()));

        Role roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));
        userService.saveUser(user);
        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
    }

}
