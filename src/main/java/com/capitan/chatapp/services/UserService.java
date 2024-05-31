package com.capitan.chatapp.services;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.capitan.chatapp.dto.LoginResponseDto;
import com.capitan.chatapp.dto.FriendDto;
import com.capitan.chatapp.dto.FriendIsOnlineDto;
import com.capitan.chatapp.dto.LoginDto;
import com.capitan.chatapp.dto.RegisterDto;
import com.capitan.chatapp.dto.SearchUserResponseDto;
import com.capitan.chatapp.dto.UpdateFirstLoginResponseDto;
import com.capitan.chatapp.dto.UpdateProfileImgDto;
import com.capitan.chatapp.dto.UpdateProfileResponseDto;
import com.capitan.chatapp.models.Friendship;
import com.capitan.chatapp.models.FriendshiptStatus;
import com.capitan.chatapp.models.Notification;
import com.capitan.chatapp.models.Role;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.FriendshipRepository;
import com.capitan.chatapp.repository.RoleRepository;
import com.capitan.chatapp.repository.UserRepository;
import com.capitan.chatapp.security.JwtGenerator;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

@Service
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtGenerator jwtGenerator;
    private AuthenticationManager authenticationManager;
    private SimpMessagingTemplate simpMessagingTemplate;
    private FriendshipRepository friendshipRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator, AuthenticationManager authenticationManager,
            FriendshipRepository friendshipRepository,
            SimpMessagingTemplate simpMessagingTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.authenticationManager = authenticationManager;
        this.friendshipRepository = friendshipRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;

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
            System.out.println(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public void updateOnlineStatus(String username, boolean onlineStatus) {
        userRepository.updateOnlineStatus(username, onlineStatus);
    }

    public ResponseEntity<?> deleteUserByUsername(HttpServletRequest request) {
        try {
            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
            if (op.isPresent()) {
                String usernameToDelete = op.get().getUsername();
                userRepository.deleteUserByUsername(usernameToDelete);
                return new ResponseEntity<>("User deleted successfully!", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> checkIsFriendOnlineByNickname(String nickname) {
        try {
            FriendIsOnlineDto friendIsOnlineDto = userRepository.isFriendOnline(nickname);
            return new ResponseEntity<>(friendIsOnlineDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> updateProfileImage(UpdateProfileImgDto profileImgDto, HttpServletRequest request) {
        try {
            String opName = jwtGenerator.getUserNameFromJWTCookies(request);
            Optional<UserEntity> op = userRepository.findByUsername(opName);
            if (op.isPresent()) {
                UserEntity opUser = op.get();
                userRepository.updateProfileImage(opName, profileImgDto.getImagePath(), false);
                UpdateProfileResponseDto responseDto = new UpdateProfileResponseDto(profileImgDto.getImagePath(),
                        false);
                Optional<List<FriendDto>> friendsOptional = friendshipRepository.getOnlineFriends(opUser.getId());

                friendsOptional.ifPresent(friends -> {
                    List<String> onlineFriendNames = friends.stream()
                            .map(FriendDto::getNickname)
                            .collect(Collectors.toList());

                    onlineFriendNames.forEach(name -> {
                        FriendIsOnlineDto friend = new FriendIsOnlineDto(profileImgDto.getImagePath(),
                                opUser.getNickname(),
                                true);
                        Notification notification = new Notification(
                                opUser.getNickname() + " Updated its profile image",
                                com.capitan.chatapp.models.MessageType.FRIEND_UPDATED_IMG, friend);
                        simpMessagingTemplate.convertAndSendToUser(name, "/queue/notifications",
                                notification);

                    });

                });

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
            String opUsername = jwtGenerator.getUserNameFromJWTCookies(request);
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

    public ResponseEntity<?> searchUsersByNicknamePrefix(String prefix, HttpServletRequest request) {
        try {
            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
            if (op.isPresent()) {
                String searcherNickname = op.get().getNickname();
                Integer searcherId = op.get().getId();
                Optional<List<SearchUserResponseDto>> searchedUsers = userRepository
                        .findByNicknamePrefix("%" + prefix + "%", searcherNickname);

                if (searchedUsers.isPresent()) {
                    List<SearchUserResponseDto> responseDtos = searchedUsers.get().stream().map(dto -> {
                        UserEntity user = userRepository.findById(dto.getUserId()).orElse(null);
                        if (user == null) {
                            return null;
                        }
                        FriendshiptStatus friendshipStatus = FriendshiptStatus.NOT_FRIENDS;
                        Integer friendRequestId = null;
                        for (Friendship sentRequest : user.getFriendshipsAsSender()) {
                            if (sentRequest.getReceiverEntity().getId().equals(searcherId)) {
                                friendRequestId = sentRequest.getId();
                                if (sentRequest.getStatus().equals(FriendshiptStatus.FRIENDS)) {
                                    friendshipStatus = FriendshiptStatus.FRIENDS;
                                } else {
                                    friendshipStatus = FriendshiptStatus.PENDING;
                                }
                                break;
                            } else if (sentRequest.getSenderEntity().getId().equals(searcherId)) {
                                friendRequestId = sentRequest.getId();
                                if (sentRequest.getStatus().equals(FriendshiptStatus.FRIENDS)) {
                                    friendshipStatus = FriendshiptStatus.FRIENDS;
                                } else {
                                    friendshipStatus = FriendshiptStatus.WAITING;
                                }
                                break;
                            }
                        }
                        for (Friendship receivedRequest : user.getFriendshipsAsReciever()) {
                            if (receivedRequest.getSenderEntity().getId().equals(searcherId)) {
                                friendRequestId = receivedRequest.getId();
                                if (receivedRequest.getStatus().equals(FriendshiptStatus.FRIENDS)) {
                                    friendshipStatus = FriendshiptStatus.FRIENDS;
                                } else {
                                    friendshipStatus = FriendshiptStatus.WAITING;
                                }
                                break;
                            }
                        }
                        dto.setStatus(friendshipStatus);
                        dto.setRequestId(friendRequestId);
                        return dto;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

                    return new ResponseEntity<>(responseDtos, HttpStatus.OK);
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

}