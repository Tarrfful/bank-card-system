package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody CreateUserRequestDto requestDto) {
        User newUser = userService.createUser(requestDto);
        UserResponseDto responseDto = userMapper.toDto(newUser);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}