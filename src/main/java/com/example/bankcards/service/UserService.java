package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.entity.User;

public interface UserService {
    User createUser(CreateUserRequestDto request);
}