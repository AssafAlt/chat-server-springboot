package com.capitan.chatapp.services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.capitan.chatapp.dto.FriendDto;
import com.capitan.chatapp.dto.FriendIsOnlineDto;
import com.capitan.chatapp.dto.FriendRequestDto;
import com.capitan.chatapp.dto.FriendRequestOpDto;
import com.capitan.chatapp.dto.GetFriendRequestDto;
import com.capitan.chatapp.models.Friendship;
import com.capitan.chatapp.models.FriendshiptStatus;
import com.capitan.chatapp.models.Notification;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.FriendshipRepository;
import com.capitan.chatapp.repository.UserRepository;
import com.capitan.chatapp.security.JwtGenerator;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class FriendshipService {

    private FriendshipRepository friendshipRepository;
    private UserRepository userRepository;

    private JwtGenerator jwtGenerator;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public FriendshipService(
            FriendshipRepository friendshipRepository,
            UserRepository userRepository,

            JwtGenerator jwtGenerator, SimpMessagingTemplate simpMessagingTemplate) {

        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.jwtGenerator = jwtGenerator;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public ResponseEntity<String> sendFriendRequest(@RequestBody FriendRequestDto friendRequestDto,
            HttpServletRequest request) {
        try {
            Optional<UserEntity> sender = userRepository
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));

            Optional<UserEntity> reciever = userRepository.findById(friendRequestDto.getRecieverId());

            if (sender.isPresent() && reciever.isPresent()) {
                UserEntity senderUser = sender.get();
                UserEntity recieverUser = reciever.get();

                Friendship friendRequest = new Friendship();

                friendRequest.setDate(LocalDate.now());

                friendRequest.setSenderEntity(senderUser);
                friendRequest.setReceiverEntity(recieverUser);
                friendRequest.setStatus(FriendshiptStatus.PENDING);

                friendshipRepository.save(friendRequest);
                Boolean isUserOnline = userRepository.isUserOnline(recieverUser.getId());
                if (isUserOnline) {
                    GetFriendRequestDto fRequest = friendshipRepository.getOnlineFriendRequestDetailsByReceiverId(
                            recieverUser.getId(),
                            senderUser.getId());

                    Notification notification = new Notification(
                            senderUser.getNickname() + " Sent you a friend request!",
                            com.capitan.chatapp.models.MessageType.NEW_FRIEND_REQUEST, fRequest);
                    simpMessagingTemplate.convertAndSendToUser(recieverUser.getNickname(), "/queue/notifications",
                            notification);
                }
                return new ResponseEntity<>("Request was sent successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Request was failed!", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<String> cancelFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        try {
            int friendRequestIdToDelete = friendRequestOpDto.getFriendRequestId();
            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
            Optional<Friendship> fRequest = friendshipRepository.findById(friendRequestIdToDelete);

            if (op.isPresent() && fRequest.isPresent()) {
                Friendship friendRequest = fRequest.get();
                UserEntity opUser = op.get();
                if (opUser.getId() == friendRequest.getSenderEntity().getId()
                        || opUser.getId() == friendRequest.getReceiverEntity().getId()) {
                    friendshipRepository.deleteById(friendRequestIdToDelete);
                    UserEntity recieverUser = friendRequest.getReceiverEntity();
                    int recieverId = recieverUser.getId();
                    Boolean isUserOnline = userRepository.isUserOnline(recieverId);
                    if (isUserOnline && recieverId != opUser.getId()) {

                        Notification notification = new Notification(
                                "",
                                com.capitan.chatapp.models.MessageType.REQUEST_CANCELLED, friendRequest.getId());
                        simpMessagingTemplate.convertAndSendToUser(recieverUser.getNickname(), "/queue/notifications",
                                notification);
                    }
                    return new ResponseEntity<>("Request was deleted successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("User unauthorized for this operation", HttpStatus.UNAUTHORIZED);
                }

            } else {
                return new ResponseEntity<>("User or request wasn't found", HttpStatus.NOT_FOUND);
            }

        } catch (

        Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<String> deleteFriendship(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        try {
            int friendshipIdToDelete = friendRequestOpDto.getFriendRequestId();
            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
            Optional<Friendship> fShip = friendshipRepository.findById(friendshipIdToDelete);

            if (op.isPresent() && fShip.isPresent()) {
                Friendship friendship = fShip.get();
                UserEntity opUser = op.get();
                String userNicknameToNotify;
                Integer userIdToNotify;
                if (opUser.getId() == friendship.getReceiverEntity().getId()) {
                    userNicknameToNotify = friendship.getSenderEntity().getNickname();
                    userIdToNotify = friendship.getSenderEntity().getId();

                } else {
                    userNicknameToNotify = friendship.getReceiverEntity().getNickname();
                    userIdToNotify = friendship.getReceiverEntity().getId();

                }
                friendshipRepository.deleteById(friendshipIdToDelete);

                Boolean isUserOnline = userRepository.isUserOnline(userIdToNotify);
                if (isUserOnline) {

                    FriendIsOnlineDto friend = new FriendIsOnlineDto(opUser.getProfileImg(), opUser.getNickname(),
                            true);
                    Notification notification = new Notification(
                            "",
                            com.capitan.chatapp.models.MessageType.FRIENDSHIP_DELETED, friend);
                    simpMessagingTemplate.convertAndSendToUser(userNicknameToNotify, "/queue/notifications",
                            notification);
                }
                return new ResponseEntity<>("Request was deleted successfully", HttpStatus.OK);
            }

            else {
                return new ResponseEntity<>("User or request wasn't found", HttpStatus.NOT_FOUND);
            }

        } catch (

        Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @Transactional
    public ResponseEntity<String> confirmFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        try {
            int friendRequestId = friendRequestOpDto.getFriendRequestId();

            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
            Optional<Friendship> fRequest = friendshipRepository.findById(friendRequestId);

            if (op.isPresent() && fRequest.isPresent()) {
                Friendship friendRequest = fRequest.get();
                UserEntity opUser = op.get();
                UserEntity senderUser = friendRequest.getSenderEntity();
                if (opUser.getId() == friendRequest.getReceiverEntity().getId()) {

                    friendshipRepository.updateStatusById(friendRequestId, FriendshiptStatus.FRIENDS);
                    Boolean isUserOnline = userRepository.isUserOnline(senderUser.getId());
                    if (isUserOnline) {
                        FriendIsOnlineDto friend = new FriendIsOnlineDto(opUser.getProfileImg(), opUser.getNickname(),
                                true);
                        Notification notification = new Notification(
                                opUser.getNickname() + " Approved your friend request!",
                                com.capitan.chatapp.models.MessageType.REQUEST_APPROVED, friend);
                        simpMessagingTemplate.convertAndSendToUser(senderUser.getNickname(), "/queue/notifications",
                                notification);
                    }

                    return new ResponseEntity<>("Request was confirmed successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("User unauthorized for this operation", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return new ResponseEntity<>("User or request wasn't found", HttpStatus.NOT_FOUND);
            }

        } catch (

        Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<?> getFriendRequests(HttpServletRequest request) {
        try {

            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
            if (op.isPresent()) {
                UserEntity opUser = op.get();
                List<GetFriendRequestDto> frequests = opUser.getFriendRequests();

                return new ResponseEntity<>(frequests, HttpStatus.OK);

            } else

            {
                return new ResponseEntity<>("User or request wasn't found", HttpStatus.NOT_FOUND);
            }

        } catch (

        Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> getFriends(HttpServletRequest request) {
        try {

            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
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
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
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
                    .findByUsername(jwtGenerator.getUserNameFromJWTCookies(request));
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

}
