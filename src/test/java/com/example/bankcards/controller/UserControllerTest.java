package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UserController.class, GlobalExceptionHandler.class})class UserControllerTest {
@Import(SecurityConfig.class)
    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public UserMapper userMapper() {
            return mock(UserMapper.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    void registerUser_whenValidInput_shouldReturn201Created() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto();
        requestDto.setUsername("testuser");
        requestDto.setPassword("password1234");

        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setUsername("testuser");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setUsername("testuser");

        when(userService.createUser(any(CreateUserRequestDto.class))).thenReturn(createdUser);
        when(userMapper.toDto(any(User.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void registerUser_whenUsernameExists_shouldReturn409Conflict() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto();
        requestDto.setUsername("existingUser");
        requestDto.setPassword("password1234");

        when(userService.createUser(any(CreateUserRequestDto.class)))
                .thenThrow(new UserAlreadyExistsException("User with username 'existingUser' already exists."));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with username 'existingUser' already exists."));
    }

    @Test
    void registerUser_whenInvalidInput_shouldReturn400BadRequest() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto();
        requestDto.setUsername("u");
        requestDto.setPassword("123");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").exists());
    }
}