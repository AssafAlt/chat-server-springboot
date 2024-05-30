package com.capitan.chatapp.dto;

import com.capitan.chatapp.models.FriendshiptStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchUserResponseDto {
    private int userId;
    private String profileImg;
    private String nickname;
    private FriendshiptStatus status;
    private Integer requestId;

    public SearchUserResponseDto(int userId, String profileImg, String nickname) {
        this.userId = userId;
        this.profileImg = profileImg;
        this.nickname = nickname;
    }

}
