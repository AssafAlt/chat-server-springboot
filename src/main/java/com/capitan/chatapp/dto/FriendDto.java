package com.capitan.chatapp.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FriendDto {
    private int id;
    private String profileImg;
    private String nickname;
    @DateTimeFormat(pattern = "MM-dd-yyyy")
    private LocalDate date;
}