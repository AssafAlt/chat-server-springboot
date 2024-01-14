package com.capitan.chatapp.helpers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.capitan.chatapp.dto.SearchUserResponseDto;
import com.capitan.chatapp.models.UserEntity;

@Component
public class UserHelper {
        public List<SearchUserResponseDto> mapUserEntitiesToSearchUserResponses(List<UserEntity> userEntities) {
                return userEntities.stream()
                                .map(userEntity -> new SearchUserResponseDto(userEntity.getId(),
                                                userEntity.getProfileImg(),
                                                userEntity.getNickname()))
                                .collect(Collectors.toList());
        }

}
