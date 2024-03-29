package com.capitan.chatapp.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.capitan.chatapp.dto.LoginResponseDto;
import com.capitan.chatapp.dto.LoginDto;
import com.capitan.chatapp.dto.RegisterDto;
import com.capitan.chatapp.dto.SearchUserResponseDto;
import com.capitan.chatapp.dto.UpdateFirstLoginResponseDto;
import com.capitan.chatapp.dto.UpdateProfileImgDto;
import com.capitan.chatapp.dto.UpdateProfileResponseDto;
import com.capitan.chatapp.models.Role;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.RoleRepository;
import com.capitan.chatapp.repository.UserRepository;
import com.capitan.chatapp.security.JwtGenerator;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

            Optional<UserEntity> user = userRepository.findByUsername(loginDto.getUsername());
            if (user.isPresent()) {
                Cookie jwtCookie = jwtGenerator.generateCookie(authentication);
                response.addCookie(jwtCookie);
                UserEntity currentUser = user.get();
                LoginResponseDto loginResponseDto = new LoginResponseDto(currentUser.getNickname(),
                        currentUser.getProfileImg(), currentUser.isFirstLogin());
                return new ResponseEntity<>(loginResponseDto, HttpStatus.OK);
            } else
                return new ResponseEntity<>("User wasn't found", HttpStatus.NOT_FOUND);

        } catch (Exception e) {

            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<String> register(RegisterDto registerDto) {
        try {
            if (userRepository.existsByUsername((registerDto.getUsername()))) {
                return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
            }

            if (registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
                UserEntity user = new UserEntity();
                user.setUsername(registerDto.getUsername());
                user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
                user.setProfileImg(
                        registerDto.getProfileImg());
                user.setNickname(user.generateEncodedNickname(registerDto.getNickname()));
                user.setFirstLogin(true);

                Role roles = roleRepository.findByName("USER").get();
                user.setRoles(Collections.singletonList(roles));
                userRepository.save(user);

                return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Password and confirm password aren't equal", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> searchUsersByNicknamePrefix(String prefix, HttpServletRequest request) {
        try {
            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            if (op.isPresent()) {
                String searcherNickname = op.get().getNickname();
                Optional<List<SearchUserResponseDto>> searchedUsers = userRepository
                        .findByNicknamePrefix("%" + prefix + "%", searcherNickname);

                if (searchedUsers.isPresent()) {

                    return new ResponseEntity<>(searchedUsers.get(), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("There is no matching result", HttpStatus.OK);
                }

            } else {
                return new ResponseEntity<>("Unauthorized operation", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> updateProfileImage(UpdateProfileImgDto profileImgDto, HttpServletRequest request) {
        try {
            String opUsername = jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request));
            if (userRepository.existsByUsername(opUsername)) {
                userRepository.updateProfileImage(opUsername, profileImgDto.getImagePath(), false);
                UpdateProfileResponseDto responseDto = new UpdateProfileResponseDto(profileImgDto.getImagePath(),
                        false);

                return new ResponseEntity<>(responseDto, HttpStatus.OK);

            } else {
                return new ResponseEntity<>("Unauthorized operation", HttpStatus.UNAUTHORIZED);
            }
        } catch (

        Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> updateProfileFirstLogin(HttpServletRequest request) {
        try {
            String opUsername = jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request));
            if (userRepository.existsByUsername(opUsername)) {
                userRepository.updateProfileFirstLogin(opUsername, false);
                UpdateFirstLoginResponseDto responseDto = new UpdateFirstLoginResponseDto(false);

                return new ResponseEntity<>(responseDto, HttpStatus.OK);

            } else {
                return new ResponseEntity<>("Unauthorized operation", HttpStatus.UNAUTHORIZED);
            }
        } catch (

        Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
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

    private String getJWTFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}