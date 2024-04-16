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

    @Query("SELECT New com.capitan.chatapp.dto.GetFriendRequestDto(fr.id,fr.senderEntity.profileImg,fr.senderEntity.nickname,fr.date) FROM FriendRequest fr WHERE fr.receiverEntity.id = :userId  AND fr.status = 'PENDING'")
    Optional<List<GetFriendRequestDto>> findFriendRequestsDetailsByReceiverId(@Param("userId") int userId);

    @Query("SELECT New com.capitan.chatapp.dto.GetFriendRequestDto(fr.id,fr.senderEntity.profileImg,fr.senderEntity.nickname,fr.date) FROM FriendRequest fr WHERE fr.receiverEntity.id = :recieverId AND fr.senderEntity.id = :senderId  AND fr.status = 'PENDING'")
    GetFriendRequestDto getOnlineFriendRequestDetailsByReceiverId(@Param("recieverId") int recieverId,
            @Param("senderId") int senderId);

    @Query("SELECT fr.senderEntity.id FROM FriendRequest fr WHERE fr.receiverEntity.id = :userId")
    Optional<List<Integer>> findFriendRequestsIdByReceiverId(@Param("userId") int userId);

}
