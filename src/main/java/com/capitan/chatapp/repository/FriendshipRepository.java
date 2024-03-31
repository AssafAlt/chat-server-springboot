package com.capitan.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitan.chatapp.dto.FriendDto;
import com.capitan.chatapp.models.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    @Query("SELECT New com.capitan.chatapp.dto.FriendDto(f.id, " +
            "CASE WHEN f.user1.id = :userId THEN f.user2.profileImg ELSE f.user1.profileImg END, " +
            "CASE WHEN f.user1.id = :userId THEN f.user2.nickname ELSE f.user1.nickname END, " +
            "f.date) " +
            "FROM Friendship f " +
            "WHERE f.user1.id = :userId OR f.user2.id = :userId")
    Optional<List<FriendDto>> getFriends(@Param("userId") int userId);

    @Query("SELECT NEW com.capitan.chatapp.dto.FriendDto(f.id, " +
            "CASE WHEN f.user1.id = :userId THEN f.user2.profileImg ELSE f.user1.profileImg END, " +
            "CASE WHEN f.user1.id = :userId THEN f.user2.nickname ELSE f.user1.nickname END, " +
            "f.date) " +
            "FROM Friendship f " +
            "WHERE (f.user1.id = :userId OR f.user2.id = :userId) " +
            "AND (f.user1.isOnline = true OR f.user2.isOnline = true)")
    Optional<List<FriendDto>> getOnlineFriends(@Param("userId") int userId);

}
