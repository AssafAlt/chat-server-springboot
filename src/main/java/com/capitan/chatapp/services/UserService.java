package com.capitan.chatapp.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.capitan.chatapp.dto.AuthResponseDto;
import com.capitan.chatapp.dto.LoginDto;
import com.capitan.chatapp.dto.RegisterDto;
import com.capitan.chatapp.dto.SearchedUserResponseDto;
import com.capitan.chatapp.models.Role;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.RoleRepository;
import com.capitan.chatapp.repository.UserRepository;
import com.capitan.chatapp.security.JwtGenerator;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.Cookie;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtGenerator jwtGenerator;
    private AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsername(),
                            loginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Cookie jwtCookie = jwtGenerator.generateCookie(authentication);
            response.addCookie(jwtCookie);

            String profileImagePath = userRepository.getProfileImageByUsername(loginDto.getUsername());
            AuthResponseDto authResponse = new AuthResponseDto(jwtCookie.getValue(), profileImagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", jwtCookie.toString());

            return new ResponseEntity<>(authResponse, headers, HttpStatus.OK);
        } catch (Exception e) {

            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<String> register(RegisterDto registerDto) {
        try {
            if (userRepository.existsByUsername((registerDto.getUsername()))) {
                return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
            }

            UserEntity user = new UserEntity();
            user.setUsername(registerDto.getUsername());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setProfileImg(registerDto.getProfileImg());
            user.setNickname(user.generateEncodedNickname(registerDto.getNickname()));

            Role roles = roleRepository.findByName("USER").get();
            user.setRoles(Collections.singletonList(roles));
            userRepository.save(user);

            return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<List<SearchedUserResponseDto>> searchUsersByNickname(String partialNickname) {
        List<SearchedUserResponseDto> searchedUsers = userRepository.searchUsersByPartialNickname(partialNickname);

        List<SearchedUserResponseDto> responseDtoList = searchedUsers.stream()
                .map(user -> new SearchedUserResponseDto(user.getUserId(), user.getUserImage(), user.getUserNickname()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(responseDtoList, HttpStatus.OK);
    }

    public String getProfileImageByUsername(String username) {
        return userRepository.getProfileImageByUsername(username);
    }

    public Optional<UserEntity> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public String getNicknameByUsername(String username) {
        return userRepository.getNicknameByUsername(username);
    }

    public Boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}