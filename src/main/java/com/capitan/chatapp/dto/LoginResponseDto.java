package com.capitan.chatapp.dto;

import lombok.Data;

@Data
public class LoginResponseDto {

    private String userNickname;
    private String imagePath;
    private boolean isFirstLogin;

    public LoginResponseDto(String userNickname, String imagePath, boolean isFirstLogin) {
        this.userNickname = userNickname;
        this.imagePath = imagePath;
        this.isFirstLogin = isFirstLogin;

    }

}
