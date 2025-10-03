package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User createUser(CreateUserRequestDto request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("User with username '" + request.getUsername() + "' already exists.");
                });

        User newUser = new User();
        newUser.setUsername(request.getUsername());

        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Error: Role 'USER' is not found."));

        newUser.setRoles(new HashSet<>(Collections.singleton(userRole)));

        return userRepository.save(newUser);
    }
}