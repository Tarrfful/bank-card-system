package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User createUser(CreateUserRequestDto request);

    Page<User> getAllUsers(Pageable pageable);

    User assignAdminRole(Long userId);

    User removeAdminRole(Long userId);
}