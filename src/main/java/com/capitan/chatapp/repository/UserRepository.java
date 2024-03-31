package com.capitan.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitan.chatapp.dto.SearchUserResponseDto;
import com.capitan.chatapp.models.UserEntity;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
        Optional<UserEntity> findByUsername(String username);

        Optional<UserEntity> findByNickname(String nickname);

        @Query("SELECT u.id FROM UserEntity u WHERE u.username = :username")
        Optional<Integer> findUserIdByUsername(@Param("username") String username);

        /*
         * @Query("SELECT NEW UserEntity(u.id,u.profileImg, u.nickname) " +
         * "FROM UserEntity u " +
         * "WHERE LOWER(u.nickname) LIKE LOWER(:prefix) " +
         * "AND u.nickname <> :searcherNickname")
         * Optional<List<UserEntity>> findByNicknamePrefix(
         * 
         * @Param("prefix") String prefix,
         * 
         * @Param("searcherNickname") String searcherNickname);
         */
        @Modifying
        @Transactional
        @Query("UPDATE UserEntity u SET u.isOnline = :isOnline WHERE u.username = :username")
        void updateOnlineStatus(@Param("username") String username, @Param("isOnline") boolean isOnline);

        @Query("SELECT NEW com.capitan.chatapp.dto.SearchUserResponseDto(u.id,u.profileImg, u.nickname) " +
                        "FROM UserEntity u " +
                        "WHERE LOWER(u.nickname) LIKE LOWER(:prefix) " +
                        "AND u.nickname <> :searcherNickname")
        Optional<List<SearchUserResponseDto>> findByNicknamePrefix(
                        @Param("prefix") String prefix,
                        @Param("searcherNickname") String searcherNickname);

        @Query("SELECT u.profileImg FROM UserEntity u WHERE u.username = ?1")
        String getProfileImageByUsername(String username);

        Boolean existsByUsername(String username);

        Boolean existsByNickname(String nickname);

        @Query("SELECT u.nickname FROM UserEntity u WHERE u.username = ?1")
        String getNicknameByUsername(String username);

        @Modifying
        @Transactional
        @Query("UPDATE UserEntity u SET u.profileImg = :profileImg, u.isFirstLogin = :isFirstLogin WHERE u.username = :username")
        void updateProfileImage(
                        @Param("username") String username,
                        @Param("profileImg") String profileImg,
                        @Param("isFirstLogin") boolean isFirstLogin);

        @Modifying
        @Transactional
        @Query("UPDATE UserEntity u SET u.isFirstLogin = :isFirstLogin WHERE u.username = :username")
        void updateProfileFirstLogin(@Param("username") String username,
                        @Param("isFirstLogin") boolean isFirstLogin);

}
