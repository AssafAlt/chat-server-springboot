package com.capitan.chatapp.dto;

import lombok.Data;

@Data
public class LoginResponseDto {

    private String userNickname;
    private String imagePath;

    public LoginResponseDto(String userNickname, String imagePath) {
        this.userNickname = userNickname;
        this.imagePath = imagePath;

    }

}
