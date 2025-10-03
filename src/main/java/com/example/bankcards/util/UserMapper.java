package com.example.bankcards.util;

import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}