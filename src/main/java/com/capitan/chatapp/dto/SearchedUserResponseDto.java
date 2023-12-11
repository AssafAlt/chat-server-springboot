package com.capitan.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchedUserResponseDto {
    private int userId;
    private String userImage;
    private String userNickname;
}
