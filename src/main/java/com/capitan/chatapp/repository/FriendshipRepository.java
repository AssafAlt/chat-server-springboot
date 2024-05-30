package com.capitan.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitan.chatapp.dto.FriendDto;
import com.capitan.chatapp.dto.GetFriendRequestDto;
import com.capitan.chatapp.models.Friendship;
import com.capitan.chatapp.models.FriendshiptStatus;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    @Modifying
    @Query("UPDATE Friendship fr SET fr.status = :status WHERE fr.id = :requestId")
    void updateStatusById(@Param("requestId") int requestId, @Param("status") FriendshiptStatus status);

    @Query("SELECT New com.capitan.chatapp.dto.GetFriendRequestDto(fr.id,fr.senderEntity.profileImg,fr.senderEntity.nickname,fr.date) FROM Friendship fr WHERE fr.receiverEntity.id = :userId  AND fr.status = 'PENDING'")
    Optional<List<GetFriendRequestDto>> findFriendRequestsDetailsByReceiverId(@Param("userId") int userId);

    @Query("SELECT New com.capitan.chatapp.dto.GetFriendRequestDto(fr.id,fr.senderEntity.profileImg,fr.senderEntity.nickname,fr.date) FROM Friendship fr WHERE fr.receiverEntity.id = :recieverId AND fr.senderEntity.id = :senderId  AND fr.status = 'PENDING'")
    GetFriendRequestDto getOnlineFriendRequestDetailsByReceiverId(@Param("recieverId") int recieverId,
            @Param("senderId") int senderId);

    @Query("SELECT fr.senderEntity.id FROM Friendship fr WHERE fr.receiverEntity.id = :userId")
    Optional<List<Integer>> findFriendRequestsIdByReceiverId(@Param("userId") int userId);

    @Query("SELECT New com.capitan.chatapp.dto.FriendDto(f.id, " +
            "CASE WHEN f.senderEntity.id = :userId THEN f.receiverEntity.profileImg ELSE f.senderEntity.profileImg END, "
            +
            "CASE WHEN f.senderEntity.id = :userId THEN f.receiverEntity.nickname ELSE f.senderEntity.nickname END, " +
            "f.date) " +
            "FROM Friendship f " +
            "WHERE f.senderEntity.id = :userId OR f.receiverEntity.id = :userId")
    Optional<List<FriendDto>> getFriends(@Param("userId") int userId);

    @Query("SELECT NEW com.capitan.chatapp.dto.FriendDto(f.id, " +
            "CASE WHEN f.senderEntity.id = :userId THEN f.receiverEntity.profileImg ELSE f.senderEntity.profileImg END, "
            +
            "CASE WHEN f.senderEntity.id = :userId THEN f.receiverEntity.nickname ELSE f.senderEntity.nickname END, " +
            "f.date) " +
            "FROM Friendship f " +
            "WHERE (f.senderEntity.id = :userId OR f.receiverEntity.id = :userId) " +
            "AND (f.senderEntity.isOnline = true OR f.receiverEntity.isOnline = true)")
    Optional<List<FriendDto>> getOnlineFriends(@Param("userId") int userId);

    @Query("SELECT New com.capitan.chatapp.dto.FriendDto(f.id, " +
            "CASE WHEN f.senderEntity.id = :userId THEN f.receiverEntity.profileImg ELSE f.senderEntity.profileImg END, "
            +
            "CASE WHEN f.senderEntity.id = :userId THEN f.receiverEntity.nickname ELSE f.senderEntity.nickname END, " +
            "CASE WHEN f.senderEntity.id = :userId THEN f.receiverEntity.isOnline ELSE f.senderEntity.isOnline END, " +
            "f.date) " +
            "FROM Friendship f " +
            "WHERE f.senderEntity.id = :userId OR f.receiverEntity.id = :userId")
    Optional<List<FriendDto>> getFriendsWithStatus(@Param("userId") int userId);

}
