package com.capitan.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitan.chatapp.dto.GetFriendRequestDto;
import com.capitan.chatapp.models.FriendRequest;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    @Modifying
    @Query("UPDATE FriendRequest fr SET fr.status = :status WHERE fr.id = :requestId")
    void updateStatusById(@Param("requestId") int requestId, @Param("status") String status);

    Optional<List<FriendRequest>> findByReceiverEntity_Id(int userId);

    @Query("SELECT new com.capitan.chatapp.dto.GetFriendRequestDto(fr.senderEntity.profileImg, fr.senderEntity.nickname, fr.date) "
            +
            "FROM FriendRequest fr " +
            "WHERE fr.receiverEntity.id = :userId")
    Optional<List<GetFriendRequestDto>> findFriendRequestsDetailsByReceiverId(int userId);
}
