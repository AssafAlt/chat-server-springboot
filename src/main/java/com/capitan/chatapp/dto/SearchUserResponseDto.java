package com.capitan.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchUserResponseDto {
    private int userId;
    private String profileImg;
    private String nickname;

}
