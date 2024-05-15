package com.capitan.chatapp.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.capitan.chatapp.dto.FriendDto;
import com.capitan.chatapp.models.Friendship;
import com.capitan.chatapp.models.UserEntity;

import com.capitan.chatapp.repository.FriendshipRepository;
import com.capitan.chatapp.repository.UserRepository;
import com.capitan.chatapp.security.JwtGenerator;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class FriendshipService {

    private UserRepository userRepository;
    private FriendshipRepository friendshipRepository;
    private JwtGenerator jwtGenerator;

    public FriendshipService(

            UserRepository userRepository,
            FriendshipRepository friendshipRepository,
            JwtGenerator jwtGenerator) {

        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.jwtGenerator = jwtGenerator;
    }

    public void saveFriendship(Friendship friendship) {
        friendshipRepository.save(friendship);
    }

    public ResponseEntity<?> getFriends(HttpServletRequest request) {
        try {

            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            if (op.isPresent()) {
                UserEntity opUser = op.get();
                Optional<List<FriendDto>> friends = friendshipRepository
                        .getFriends(opUser.getId());
                if (friends.isPresent()) {
                    return new ResponseEntity<>(friends.get(), HttpStatus.OK);

                } else {
                    return new ResponseEntity<>("There are no friends", HttpStatus.NO_CONTENT);
                }
            } else

            {
                return new ResponseEntity<>("User or request wasn't found", HttpStatus.NOT_FOUND);
            }

        } catch (

        Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> getOnlineFriends(HttpServletRequest request) {
        try {

            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            if (op.isPresent()) {
                UserEntity opUser = op.get();
                Optional<List<FriendDto>> friends = friendshipRepository
                        .getOnlineFriends(opUser.getId());
                if (friends.isPresent()) {
                    return new ResponseEntity<>(friends.get(), HttpStatus.OK);

                } else {
                    return new ResponseEntity<>("There are no online friends", HttpStatus.NO_CONTENT);
                }
            } else

            {
                return new ResponseEntity<>("User or request wasn't found", HttpStatus.NOT_FOUND);
            }

        } catch (

        Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> getFriendsWithStatus(HttpServletRequest request) {
        try {
            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            if (op.isPresent()) {
                UserEntity opUser = op.get();
                Optional<List<FriendDto>> optionalFriends = friendshipRepository.getFriendsWithStatus(opUser.getId());
                if (optionalFriends.isPresent()) {

                    Map<String, String> onlineFriendsMap = new HashMap<>();
                    Map<String, String> offlineFriendsMap = new HashMap<>();

                    for (FriendDto friend : optionalFriends.get()) {
                        if (friend.getIsOnline()) {
                            onlineFriendsMap.put(friend.getNickname(), friend.getProfileImg());
                        } else {
                            offlineFriendsMap.put(friend.getNickname(), friend.getProfileImg());
                        }
                    }

                    Map<String, Map<String, String>> response = new HashMap<>();
                    response.put("onlineFriends", onlineFriendsMap);
                    response.put("offlineFriends", offlineFriendsMap);

                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("There are no online friends", HttpStatus.NO_CONTENT);
                }
            } else {
                return new ResponseEntity<>("User or request wasn't found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
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
