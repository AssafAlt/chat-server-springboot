package com.capitan.chatapp.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FriendDto {
    private Integer id;
    private String profileImg;
    private String nickname;
    private Boolean isOnline;
    @DateTimeFormat(pattern = "MM-dd-yyyy")
    private LocalDate date;

    public FriendDto(Integer id, String profileImg, String nickname, LocalDate date) {
        this.id = id;
        this.profileImg = profileImg;
        this.nickname = nickname;
        this.date = date;
    }

}