package com.capitan.chatapp.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String username;
    private String password;
    private String nickname;
    private String profileImg;
}
