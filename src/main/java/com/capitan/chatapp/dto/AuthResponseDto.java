package com.capitan.chatapp.dto;

import lombok.Data;

@Data
public class AuthResponseDto {

    private String accessToken;
    private String tokenType = "Bearer ";
    private String imagePath;

    public AuthResponseDto(String accessToken, String imagePath) {
        this.accessToken = accessToken;
        this.imagePath = imagePath;
    }

}
