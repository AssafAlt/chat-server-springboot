package com.capitan.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FriendIsOnlineDto {
    private String profileImg;
    private String nickname;
    private Boolean isOnline;
}
