package com.capitan.chatapp.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.capitan.chatapp.dto.FriendIsOnlineDto;
import com.capitan.chatapp.dto.FriendRequestDto;
import com.capitan.chatapp.dto.FriendRequestOpDto;
import com.capitan.chatapp.dto.GetFriendRequestDto;
import com.capitan.chatapp.models.FriendRequest;
import com.capitan.chatapp.models.Friendship;
import com.capitan.chatapp.models.Notification;
import com.capitan.chatapp.models.UserEntity;
import com.capitan.chatapp.repository.FriendRequestRepository;
import com.capitan.chatapp.repository.FriendshipRepository;
import com.capitan.chatapp.repository.UserRepository;
import com.capitan.chatapp.security.JwtGenerator;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class FriendRequestService {

    private FriendRequestRepository friendRequestsRepository;
    private UserRepository userRepository;
    private FriendshipRepository friendshipRepository;
    private JwtGenerator jwtGenerator;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public FriendRequestService(
            FriendRequestRepository friendRequestsRepository,
            UserRepository userRepository,
            FriendshipRepository friendshipRepository,
            JwtGenerator jwtGenerator, SimpMessagingTemplate simpMessagingTemplate) {
        this.friendRequestsRepository = friendRequestsRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.jwtGenerator = jwtGenerator;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public ResponseEntity<String> sendFriendRequest(@RequestBody FriendRequestDto friendRequestDto,
            HttpServletRequest request) {
        try {
            Optional<UserEntity> sender = userRepository
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));

            Optional<UserEntity> reciever = userRepository.findById(friendRequestDto.getRecieverId());

            if (sender.isPresent() && reciever.isPresent()) {
                UserEntity senderUser = sender.get();
                UserEntity recieverUser = reciever.get();

                FriendRequest friendRequest = new FriendRequest();

                friendRequest.setDate(LocalDate.now());

                friendRequest.setSenderEntity(senderUser);
                friendRequest.setReceiverEntity(recieverUser);
                friendRequest.setStatus("PENDING");

                friendRequestsRepository.save(friendRequest);
                Boolean isUserOnline = userRepository.isUserOnline(recieverUser.getId());
                if (isUserOnline) {
                    GetFriendRequestDto fRequest = friendRequestsRepository.getOnlineFriendRequestDetailsByReceiverId(
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
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            Optional<FriendRequest> fRequest = friendRequestsRepository.findById(friendRequestIdToDelete);

            if (op.isPresent() && fRequest.isPresent()) {
                FriendRequest friendRequest = fRequest.get();
                UserEntity opUser = op.get();
                if (opUser.getId() == friendRequest.getSenderEntity().getId()
                        || opUser.getId() == friendRequest.getReceiverEntity().getId()) {
                    friendRequestsRepository.deleteById(friendRequestIdToDelete);
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

    @Transactional
    public ResponseEntity<String> confirmFriendRequest(@RequestBody FriendRequestOpDto friendRequestOpDto,
            HttpServletRequest request) {
        try {
            int friendRequestId = friendRequestOpDto.getFriendRequestId();
            Friendship friendship = new Friendship();
            Optional<UserEntity> op = userRepository
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            Optional<FriendRequest> fRequest = friendRequestsRepository.findById(friendRequestId);

            if (op.isPresent() && fRequest.isPresent()) {
                FriendRequest friendRequest = fRequest.get();
                UserEntity opUser = op.get();
                UserEntity senderUser = friendRequest.getSenderEntity();
                if (opUser.getId() == friendRequest.getReceiverEntity().getId()) {
                    friendship.setDate(LocalDate.now());
                    friendship.setUser1(senderUser);
                    friendship.setUser2(opUser);
                    friendshipRepository.save(friendship);
                    friendRequestsRepository.updateStatusById(friendRequestId, "CONFIRMED");
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
                    .findByUsername(jwtGenerator.getUsernameFromJwt(getJWTFromCookies(request)));
            if (op.isPresent()) {
                UserEntity opUser = op.get();
                Optional<List<GetFriendRequestDto>> frequests = friendRequestsRepository
                        .findFriendRequestsDetailsByReceiverId(opUser.getId());
                if (frequests.isPresent()) {
                    return new ResponseEntity<>(frequests.get(), HttpStatus.OK);

                } else {
                    return new ResponseEntity<>("User unauthorized for this operation", HttpStatus.UNAUTHORIZED);
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
