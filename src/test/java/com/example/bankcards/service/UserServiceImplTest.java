package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRoles(new HashSet<>());

        userRole = new Role();
        userRole.setName("ROLE_USER");
        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
    }

    @Test
    void createUser_whenUserDoesNotExist_shouldSaveAndReturnUser() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("newUser");
        request.setPassword("password123");

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(1L);
            return userToSave;
        });

        User createdUser = userService.createUser(request);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(1L);
        assertThat(createdUser.getUsername()).isEqualTo("newUser");
        assertThat(createdUser.getPassword()).isEqualTo("hashedPassword");
        assertThat(createdUser.getRoles()).contains(userRole);

        verify(userRepository, times(1)).findByUsername("newUser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_whenUserAlreadyExists_shouldThrowException() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("existingUser");
        request.setPassword("password123");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(request);
        });

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void whenAssignAdminRole_toExistingUser_thenRoleIsAdded() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        userService.assignAdminRole(1L);

        assertTrue(testUser.getRoles().contains(adminRole));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void whenRemoveAdminRole_fromUserWithAdminRole_thenRoleIsRemoved() {
        testUser.getRoles().add(adminRole);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        userService.removeAdminRole(1L);

        assertFalse(testUser.getRoles().contains(adminRole));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void whenAssignAdminRole_toNonExistingUser_thenThrowUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.assignAdminRole(99L);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}