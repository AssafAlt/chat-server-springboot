package com.capitan.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capitan.chatapp.dto.SearchedUserResponseDto;
import com.capitan.chatapp.models.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByNickname(String nickname);

    /*
     * @Query("SELECT NEW com.capitan.chatapp.dto.SearchedUserResponseDto(u.id, u.profileImg, u.nickname) FROM UserEntity u WHERE LOWER(u.nickname) LIKE LOWER(:partialNickname)%"
     * )
     * List<SearchedUserResponseDto>
     * searchUsersByPartialNickname(@Param("partialNickname") String
     * partialNickname);
     */

    @Query("SELECT u.profileImg FROM UserEntity u WHERE u.username = ?1")
    String getProfileImageByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByNickname(String nickname);

    @Query("SELECT u.nickname FROM UserEntity u WHERE u.username = ?1")
    String getNicknameByUsername(String username);

}
