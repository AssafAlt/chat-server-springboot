package com.capitan.chatapp.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessagesHistoryResponseDto {
    private int totalPages;
    private int currentPage;
    private Map<String, List<ChatMessageResponseDto>> messagesByDate;
    private Boolean hasNext;
}
